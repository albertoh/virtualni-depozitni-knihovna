/*
 * This program is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package cz.incad.vdkcr.server.utils;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;
import cz.incad.vdkcr.server.analytics.akka.links.URLValidationMaster;
import cz.incad.vdkcr.server.analytics.akka.messages.StartAnalyze;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Pavel Stastny <pavel.stastny at gmail.com>
 */
public class TestConnectionUtils {

    public static Connection getRemoteConnection() throws ClassNotFoundException, SQLException {
        String driverClass = "oracle.jdbc.OracleDriver";
        String url = "jdbc:oracle:thin:@//oratest.incad.cz:1521/orcl";
        Class.forName(driverClass);
        return DriverManager.getConnection(url, "DEV_vdkcr", "vdkcr");
    }

    public static Connection getLocalConnection() throws ClassNotFoundException, SQLException {
        String driverClass = "oracle.jdbc.OracleDriver";
        String url = "jdbc:oracle:thin:@//localhost:1521/orcl";
        Class.forName(driverClass);
        return DriverManager.getConnection(url, "DEV_vdkcr", "vdkcr");
    }

    public static Connection getConnection() throws ClassNotFoundException, ClassNotFoundException, SQLException {
        //return getRemoteConnection();
        return getLocalConnection();
    }
}
