<Server pot="8005" shutdown="0fbb9iaebcbfbef203eca71b6be367859">

    <Service name="Tomcat-Apache">

        <Connector address="127.0.0.1"
                   port="8009"
                   minProcessors="5"
                   maxProcessors="75"
                   enableLookups="false"
                   protocol="AJP/1.3"/>

        <Engine name="appserver"
                defaultHost="wisemapping.com">

            <Host name="wisemapping.com"
                  appBase="/var/lib/tomcat6/webapps/wisemapping.com/"
                  autoDeploy="false"
                  deployOnStartup="false"
                  unpackWARs="false"
                  deployXML="false"
                  debug="0">
                <Context path=""
                         docBase="/var/lib/tomcat6/webapps/wisemapping.com/home"
                         reloadable="false"
                         debug="true">
                </Context>


            </Host>
        </Engine>

    </Service>

    <Service name="Tomcat-Apache2">

        <Connector address="127.0.0.1"
                   port="8010"
                   minProcessors="5"
                   maxProcessors="75"
                   enableLookups="false"
                   protocol="AJP/1.3"/>

        <Engine name="appserver2"
                defaultHost="app.wisemapping.com">

            <Host name="app.wisemapping.com"
                  appBase="/var/lib/tomcat6/webapps/wisemapping.com/app"
                  autoDeploy="false"
                  deployOnStartup="false"
                  unpackWARs="false"
                  deployXML="false">

                <Context path=""
                         docBase="/var/lib/tomcat6/webapps/wisemapping.com/app"
                         reloadable="false">

                    <Resource
                            name="jdbc/wiseapp"
                            auth="Container"
                            type="javax.sql.DataSource"
                            maxActive="100"
                            maxIdle="30" maxWait="10000"
                            username="wiseapp"
                            password="password"
                            driverClassName="com.mysql.jdbc.Driver"
                            url="jdbc:mysql://localhost/wiseapp"
                            validationQuery="SELECT 1"
                            testOnBorrow="true"
                            />
                </Context>


            </Host>
        </Engine>

    </Service>

</Server>
