#!/bin/sh

if [ -z "$WILDFLY_VERION" ]; then
  WILDFLY_VERION="8.1.0.Final-SNAPSHOT"
fi

if [ -z "$KEYCLOAK_HOME" ]; then
  # default value
  KEYCLOAK_HOME=$HOME/install/keycloak
fi


# ----------------------------------------------------------------------
# Determine what specific platform we are running on.
# Set some platform-specific variables.
# ----------------------------------------------------------------------

case "`uname`" in
   CYGWIN*) _CYGWIN=true
            ;;
   Linux*)  _LINUX=true
            ;;
   Darwin*) _DARWIN=true
            ;;
   SunOS*) _SOLARIS=true
            ;;
   AIX*)   _AIX=true
            ;;
esac

# only certain platforms support the -e argument for readlink
if [ -n "${_LINUX}${_SOLARIS}${_CYGWIN}" ]; then
   _READLINK_ARG="-e"
fi

_DOLLARZERO=`readlink $_READLINK_ARG "$0" 2>/dev/null || echo "$0"`
WILDFLY_HOME=`dirname "$_DOLLARZERO"`"/../build/target/wildfly-$WILDFLY_VERION/"

echo "WILDFLY_HOME=$WILDFLY_HOME"
echo "KEYCLOAK_HOME=$KEYCLOAK_HOME"

echo "Creating Management User.."
echo "-----------------------------------"
$WILDFLY_HOME/bin/add-user.sh --user managementAdmin --password managementAdmin --realm ManagementRealm

echo -e "\nCopying the Keycloak Auth Server.."
echo "-----------------------------------"
cp -r $KEYCLOAK_HOME/distribution/war-dist/target/unpacked/deployments/* $WILDFLY_HOME/standalone/deployments

echo -e "\nCopying the application configs for web-console and http-endpoint.."
echo "-----------------------------------"
cp `dirname "$_DOLLARZERO"`/../keycloak/app-config/* $WILDFLY_HOME/standalone/configuration

echo -e "\nDone."
echo "-----------------------------------"
echo ""
echo "Now go to $WILDFLY_HOME/bin:"
echo "cd $WILDFLY_HOME/bin"
echo ""
echo "and do:"
echo "./standalone.sh -Dkeycloak.migration.action=import -Dkeycloak.migration.provider=dir -Dkeycloak.migration.dir=../../../../keycloak/import"
