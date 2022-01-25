# Intro

This is an assemblyscript project which experiments with Fastly
[compute@edge](https://developer.fastly.com/learning/compute/assemblyscript).

It was built initially by running `fastly compute init` and then
customised. In order to make it easier to run and test, there is a
vagrant file so you don't actually have anything install locally
(except vagrant) if you do want to run it that way.

That leaves you the choice of either installing everything locally
using the vagrant box but then either way you can run the following
commands (locally or via `vagrant ssh` to get a session in the virtual
machine).

# Building

```shell
$ fastly compute build
```

# Running locally

```shell
$ fastly compute serve
```

This command will run a local version of the __compute@edge__ platform
listening on (by default) port 7676:

``` shell
$ curl -
```

# Deploying

# Setting up with Vagrant

To run this in a vagrant box, you can do:

```shell
$ vagrant up
[…]
$ vagrant ssh --command 'cd /vagrant; fastly compute serve --addr='0.0.0.0:7676' --watch'
```

In other words, just do a `vagrant up` and then you can use `vagrant
ssh` to login into the machine, cd to `/vagrant` and then run any of
the above `fastly` CLI commands.
