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
package org.sonatype.flexmojos.compatibilitykit;

import java.util.*;

public class VersionUtils
{

    public static int[] splitVersion( String version, int size )
    {
        int[] versions = splitVersion( version );

        if ( versions.length != size )
        {
            int[] temp = new int[size];
            Arrays.fill( temp, 0 );
            System.arraycopy( versions, 0, temp, 0, Math.min( versions.length, size ) );
            versions = temp;
        }

        return versions;
    }

    public static int[] splitVersion( String version )
    {
        if ( version == null || version.trim().equals( "" ) )
        {
            return new int[0];
        }

        if (!isLastCharDigit(version)) {
            version = version.substring(0, version.length() - 1);
        }

        int endIndex = version.indexOf( '-' );
        if ( endIndex != -1 )
        {
            version = version.substring( 0, endIndex );
        }

        String[] versionsStr = version.split( "\\." );
        int[] versions = new int[versionsStr.length];

        for ( int i = 0; i < versionsStr.length; i++ )
        {
            versions[i] = new Integer( versionsStr[i] );
        }

        return versions;
    }

    private static boolean isLastCharDigit(String version) {
        return Character.isDigit(version.charAt(version.length() - 1));
    }

    public static boolean isMinVersionOK( int[] fdkVersion, int[] minVersion )
    {
        return isVersionOK( fdkVersion, minVersion );
    }

    public static boolean isMaxVersionOK( int[] fdkVersion, int[] maxVersion )
    {
        return isVersionOK( maxVersion, fdkVersion );
    }

    private static boolean isVersionOK( int[] fdkVersion, int[] minVersion )
    {
        int lenght = getSmaller( fdkVersion.length, minVersion.length );

        int result = 0;
        for ( int i = 0; i < lenght; i++ )
        {
            result = fdkVersion[i] - minVersion[i];
            if ( result != 0 )
            {
                return result > -1;
            }
        }

        return result > -1;
    }

    private static int getSmaller( int... integers )
    {
        if ( integers.length == 0 )
        {
            return 0;
        }

        int smaller = Integer.MAX_VALUE;

        for ( int integer : integers )
        {
            if ( integer < smaller )
            {
                smaller = integer;
            }
        }

        return smaller;
    }

}
