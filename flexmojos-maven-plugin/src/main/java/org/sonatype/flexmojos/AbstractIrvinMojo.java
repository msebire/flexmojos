/**
 * Flexmojos is a set of maven goals to allow maven users to compile, optimize and test Flex SWF, Flex SWC, Air SWF and Air SWC.
 * Copyright (C) 2008-2012  Marvin Froeder <marvin@flexmojos.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sonatype.flexmojos;

import static org.sonatype.flexmojos.common.FlexExtension.SWC;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.*;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.flexmojos.utilities.MavenUtils;

/**
 * Encapsulate the access to Maven API. Some times just to hide Java 5 warnings
 */
public abstract class AbstractIrvinMojo
    extends AbstractMojo
    implements MavenMojo
{

    /**
     * @component
     */
    protected ArtifactFactory artifactFactory;

    /**
     * @component
     */
    protected ArtifactMetadataSource artifactMetadataSource;

    /**
     * @parameter expression="${project.build}"
     * @required
     * @readonly
     */
    protected Build build;

    /**
     * LW : needed for expression evaluation The maven MojoExecution needed for ExpressionEvaluation
     *
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    protected MavenSession context;

    // dependency artifactes
    private Set<Artifact> dependencyArtifacts;

    /**
     * Local repository to be used by the plugin to resolve dependencies.
     *
     * @parameter expression="${localRepository}"
     */
    protected ArtifactRepository localRepository;

    /**
     * @component
     */
    protected MavenProjectBuilder mavenProjectBuilder;

    /**
     * @parameter expression="${plugin.artifacts}"
     */
    protected List<Artifact> pluginArtifacts;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * @component
     */
    protected MavenProjectHelper projectHelper;

    /**
     * List of remote repositories to be used by the plugin to resolve dependencies.
     *
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    @SuppressWarnings( "unchecked" )
    protected List remoteRepositories;

    /**
     * @component
     */
    protected ArtifactResolver resolver;

    /**
     * @parameter default-value="false" expression="${flexmojos.skip}"
     */
    private boolean skip;

    /**
     * Construct Mojo instance
     */
    public AbstractIrvinMojo()
    {
        super();
    }

    protected static final ReentrantLock LOCK = new ReentrantLock();

    /**
     * Executes plugin
     */
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( skip )
        {
            getLog().info( "Skipping Flexmojos execution" );
            return;
        }

        setUp();
        try {
            LOCK.lock();
            run();
        } finally {
            LOCK.unlock();
        }
        tearDown();
    }

    /**
     * Returns Set of dependency artifacts which are resolved for the project.
     *
     * @return Set of dependency artifacts.
     * @throws MojoExecutionException
     */
    protected Set<Artifact> getDependencyArtifacts()
        throws MojoExecutionException
    {
        if ( dependencyArtifacts == null )
        {
            dependencyArtifacts =
                MavenUtils.getDependencyArtifacts( project, resolver, localRepository, remoteRepositories,
                                                   artifactMetadataSource, artifactFactory );
            for ( Artifact a : dependencyArtifacts )
            {
                if ( SWC.equals( a.getType() ) && //
                    ( "playerglobal".equals( a.getArtifactId() ) || "airglobal".equals( a.getArtifactId() ) ) )
                {
                    File source = a.getFile();
                    File dest =
                        new File( source.getParentFile(), a.getClassifier() + "/" + a.getArtifactId() + "." + SWC );
                    a.setFile( dest );

                    try
                    {
                        if ( !dest.exists() )
                        {
                            getLog().debug( "Striping global artifact, source: " + source + ", dest: " + dest );
                            FileUtils.copyFile( source, dest );
                        }
                    }
                    catch ( IOException e )
                    {
                        throw new MojoExecutionException( "Error renamming '" + a.getArtifactId() + "'.", e );
                    }
                }
            }
        }
        return dependencyArtifacts;
    }

    /**
     * Get dependency artifacts for given scope
     *
     * @param scopes for which to get artifacts
     * @return List of artifacts
     * @throws MojoExecutionException
     */
    protected List<Artifact> getDependencyArtifacts( String... scopes )
        throws MojoExecutionException
    {
        if ( scopes == null )
            return null;

        if ( scopes.length == 0 )
        {
            return new ArrayList<Artifact>();
        }

        List<String> scopesList = Arrays.asList( scopes );

        List<Artifact> artifacts = new ArrayList<Artifact>();
        for ( Artifact artifact : getDependencyArtifacts() )
        {
            if ( SWC.equals( artifact.getType() ) && scopesList.contains( artifact.getScope() ) )
            {
                artifacts.add( artifact );
            }
        }
        return artifacts;
    }

    public MavenSession getSession()
    {
        return context;
    }

    /**
     * Perform plugin functionality
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    protected abstract void run()
        throws MojoExecutionException, MojoFailureException;

    /**
     * Perform setup before plugin is run.
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    protected abstract void setUp()
        throws MojoExecutionException, MojoFailureException;

    /**
     * Perform (cleanup) actions after plugin has run
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    protected abstract void tearDown()
        throws MojoExecutionException, MojoFailureException;
}
