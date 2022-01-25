FASTLY_VERSION=1.6.0

apt-get -y update
apt-get -y upgrade

apt-get install -y curl nodejs npm yarnpkg

cd /tmp
curl -LO "https://github.com/fastly/cli/releases/download/v1.6.0/fastly_${FASTLY_VERSION}_linux_amd64.deb"
dpkg -i ./fastly_${FASTLY_VERSION}_linux_amd64.deb

rm fastly_${FASTLY_VERSION}_linux_amd64.deb
