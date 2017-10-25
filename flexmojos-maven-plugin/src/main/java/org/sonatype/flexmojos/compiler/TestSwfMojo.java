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
package org.sonatype.flexmojos.compiler;

import static org.sonatype.flexmojos.common.FlexExtension.SWF;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Build the SWF including all TEST libraries.
 * 
 * @author Marvin Herman Froeder (velo.br@gmail.com)
 * @since 3.5
 * @goal test-swf
 * @requiresDependencyResolution test
 * @threadSafe
 */
public class TestSwfMojo
    extends SwfMojo
{

    @Override
    protected void attachArtifact()
    {
        projectHelper.attachArtifact( project, SWF, "test", getOutput() );
    }

    @Override
    protected void configure()
        throws MojoExecutionException
    {
        super.configure();

        // add test libraries
        configuration.includeLibraries( merge( getDependenciesPath( "internal" ), getDependenciesPath( "test" ) ) );
    }

    @Override
    protected File getOutput()
    {
        return new File( build.getDirectory(), build.getFinalName() + "-test.swf" );
    }

}
