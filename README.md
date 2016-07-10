Sets [![Build Status](https://travis-ci.org/marschall/sets.svg?branch=master)](https://travis-ci.org/marschall/sets)
====

Special purpose implementations of `java.util.Set` that in the right niche use case can be much more efficient than implementations shipped with the JDK.

The implementations support serialization but this has not been optimized.

Currently includes classes:
<dl>
<dt>SmallIntegerSet</dt>
<dd>Supports <code>java.lang.Integer</code>s from <tt>0</tt> to <tt>63</tt>, uses the same amount of memory for the entire set as a single <code>java.lang.Long</code>. Also implements <code>java.util.SortedSet</code>.</dd>
</dl>

All methods are below 325 byte and should therefore HotSpot should be able to inline them if they are hot.

None of the sets or iterators are fail-fast.
