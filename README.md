## Coin Mixer Challenge

Despite some media reports, Bitcoin is not an anonymous protocol.  Instead,
it's often referred to as a pseudonymous system.  All transactions to or from
any Bitcoin address are publicly available, so Bitcoin's “anonymity” hinges on
not knowing which addresses belong to which people.  But because addresses are
so trivial to create (it’s essentially just a key pair), you can help ensure
your anonymity by using a bunch of addresses instead of just one.


A Bitcoin mixer is one way to maintain your privacy on the Bitcoin network.
Here’s how one popular mixer (https://bitmixer.io/how.html) works:
1. You provide to the mixer a list of new, unused addresses that you own.
2. The mixer provides you with a new deposit address that it owns.
3. You transfer your bitcoins to that address.
4. The mixer will detect your transfer by watching or polling the P2P
   Bitcoin network.
5. The mixer will transfer your bitcoins from the deposit address into a
   big “house account” along with all the other bitcoins currently being
   mixed.
6. Then, over some time the mixer will use the house account to dole out
   your bitcoins in smaller increments to the withdrawal addresses that you
   provided, possibly after deducting a fee.


There are a number of reasons to use a Bitcoin mixer.  For instance, if your
salary gets paid to the same Bitcoin address every two weeks, and if you buy
your morning coffee using that address, it would be fairly easy for your
barista to look up your previous transactions and figure out how much money you
make.  Using a mixer is one of the many ways to hide that transaction flow.


Bitcoin is a very difficult protocol to work with, especially for a newcomer to
cryptocurrencies, so this challenge is to create a mixer for a new, much
simpler online currency, the Jobcoin.  Jobcoins have “addresses” that are just
arbitrary strings, and there’s no mining or transaction signing - anyone can
create Jobcoins out of thin air, or send them between any two addresses.


You can access the Jobcoin interface and APIs at
http://jobcoin.projecticeland.net/nymphaeaceous


Please create a Jobcoin mixer, analogous to the Bitcoin mixer described above.
You can use any programming language, any interface (command line, website,
whatever you want), any data storage (in-memory is fine), any mixing strategy,
as long as it works.  You can collect a fee for your mixing service if you
wish.


Mixers can be incredibly complicated, and people have spent years working on
them.  Please don’t spend time making an overly complicated solution - but be
prepared to discuss what privacy vulnerabilities might exist in your mixer as
written and how you could mitigate them.


***** TO RUN EXECUTABLE *****
java -jar coinmixer.jar

The jar was compiled with Java 10 on Linux.

Main Class: CoinMixer.java 
