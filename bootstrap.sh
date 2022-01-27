FASTLY_VERSION=1.6.0

apt-get -y update
# apt-get -y upgrade
apt-get install -y curl nodejs npm yarnpkg

cd /tmp
curl -LO "https://github.com/fastly/cli/releases/download/v1.6.0/fastly_${FASTLY_VERSION}_linux_amd64.deb"
dpkg -i ./fastly_${FASTLY_VERSION}_linux_amd64.deb

rm -v fastly_${FASTLY_VERSION}_linux_amd64.deb

## nix is neccessary for installing the language server for assemblyscript:
##
##    - https://github.com/Shopify/asls
##
##    - https://nixos.wiki/wiki/Nix_Installation_Guide

apt-get -y install nix

usermod -G nix-users vagrant

NIX_PATH="nixpkgs=https://github.com/NixOS/nixpkgs/archive/release-21.11.tar.gz" su vagrant -l --whitelist-environment=NIX_PATH \
        -c 'nix-channel --add https://nixos.org/channels/nixpkgs-unstable; nix-env -i asls -f https://github.com/saulecabrera/asls/tarball/master/'

if grep -q nix-profile /home/vagrant/.profile
then
    echo "nix already setup in profile"
else
    echo >>/home/vagrant/.profile <<EOF
if [ -d "$HOME/.nix-profile/bin" ]; then
    PATH="$HOME/.nix-profile/bin:$PATH"
fi
EOF
fi

# export NIX_PATH="nixpkgs=https://github.com/NixOS/nixpkgs/archive/release-21.11.tar.gz"
