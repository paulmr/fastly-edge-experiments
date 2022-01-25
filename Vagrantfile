#!/usr/bin/env ruby

Vagrant.configure("2") do |config|
  config.vm.box = "debian/bullseye64"
  config.vm.provision :shell, path: "bootstrap.sh"

  config.vm.network "forwarded_port",
                    guest: 7676,
                    host: 7676,
                    guest_ip: "127.0.0.1",
                    host_ip: "127.0.0.1"

  config.vm.provider "virtualbox" do |v|
    v.memory = 4096
  end

end
