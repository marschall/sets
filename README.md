Sets [![Build Status](https://travis-ci.org/marschall/sets.svg?branch=master)](https://travis-ci.org/marschall/sets)
====

Special purpose implementations of `java.util.Set` that in the right niche use case can be much more efficient than implementations shipped with the JDK.

The implementations support serialization but this has not been optimized.

Currently includes classes:
<dl>
<dt>SmallIntegerSet</dt>
<dd>Supports `java.lang.Integer`s from 0 to 63, uses the same amount of memory for the entire set as a single `java.lang.Long`. Also implements `java.util.SortedSet`.</dd>
</dl>

All methods are below 325 byte and should therefore HotSpot should be able to inline them if they are hot.

None of the sets or iterators are fail-fast.
