[![Build Status](https://travis-ci.org/jillesvangurp/jsonj.svg)](https://travis-ci.org/jillesvangurp/jsonj)
[![JitPack](https://jitpack.io/v/jillesvangurp/jsonj.svg)](https://jitpack.io/#jillesvangurp/jsonj)

# Introduction

I'm not actively using/developing this any more and have been using Kotlin in the last few years. So, if you need any changes to this project, feel free to fork and DIY.

JsonJ is a fast performing addon library to Jackson for working with schemaless [json](http://www.rfc-editor.org/rfc/rfc7493.txt) in Java. JsonJ backs the very simple json types using a fluent API.

Jackson's streaming parser is used for parsing and it uses things like generics, Java 8 streams, varargs, the Collections framework and other modern Java constructs to make life easier dealing with json.

As of 2.43, JsonJ also fully supports the Jackson ObjectMapper. Simply use the jsonj types (`JsonObject`, `JsonArray`, and `JsonPrimitive`) and jackson will serialize/deserialize as you are used to. So you can mix jsonj objects this with using strongly typed model classes.

The core use case for jsonj is quickly prototyping with complex json data structures without getting bogged down in creating endless amounts of model classes. Model classes are nice if you have a stable, and well defined domain but can be a royal pain in the ass when this is not the case and your json is relatively complex and more or less schema less.

# Get JsonJ 

After lots of frustration babysitting maven central deploys; we now release instantly on jitpack: [![JitPack](https://jitpack.io/v/jillesvangurp/jsonj.svg)](https://jitpack.io/#jillesvangurp/jsonj)

Older releases may still be available on maven central. If a recent release on maven central is critical to you, ping me.

Note. always check for the latest version. I do not always update the readme.

Java 8 is required as of version 2. For Java 7 and earlier, you can use the 1.x releases.

# License

The license is the [MIT license](http://en.wikipedia.org/wiki/MIT_License), a.k.a. the expat license. The rationale for choosing this license is that I want to maximize your freedom to do whatever you want with the code while limiting my liability for the damage this code could potentially do in your hands. I do appreciate attribution but not enough to require it in the license (beyond the obligatory copyright notice).

# Examples

JsonJ has a ton of features and there's plenty more to discover beyond what is shown here.

## Parsing


JsonJ uses Jackson's streaming parser with a custom handler to parse json into JsonElement instances. This is the fastest way to parse json.

```
// Create a parser (you need just 1 instance for your application)
JsonParser parser = new JsonParser();

// Parse some json
JsonElement element = parser.parse("[1,2,3]");
// when using streams, we assume you are using UTF-8
JsonObject object = parser.parseObject(inputStream);
// or just use a reader
JsonElement element = parser.parse(reader);

// Jsonj also supports yaml, bson, hocon, and several other tree like syntaxes through Jackson's plugin infrastructure.

// for example:
YamlParser yamlParser = new YamlParser();
JsonElement anotherElement = yamlParser.parse(inputStream)
```

## Using the Jackson ObjectMapper

As of 2.43, the Jackson ObjectMapper is fully supported.

```
JsonObject myObject=...;
String serialized = objectMapper.writeValueAsString(myObject);
JsonObject deSerialized = objectMapper.readValue(serialized, JsonObject.class);
// myObject.equals(deSerialized);

```

This also means you can mix jsonj with pojos in your models:

For example, this will work as you'd hope:

```
class Foo {
    private String attribute;
    private int anotherAttribute;
    private JsonObject nestedJsonj;

    public String getAttribute() {
        return attribute;
    }
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }
    public int getAnotherAttribute() {
        return anotherAttribute;
    }
    public void setAnotherAttribute(int anotherAttribute) {
        this.anotherAttribute = anotherAttribute;
    }
    public JsonObject getNestedJsonj() {
        return nestedJsonj;
    }
    public void setNestedJsonj(JsonObject nestedJsonj) {
        this.nestedJsonj = nestedJsonj;
    }
}

Foo foo = new Foo();
foo.setAttribute("Hi wrld");
foo.setAnotherAttribute(42);
JsonObject nested = object(
    field("meaning_of_life",42),
    field("a",42.0),
    field("b",true),
    field("c",array(42,"foo",3.14,true,null)),
    field("d",object(field("a",1)))
);
foo.setNestedJsonj(nested);

String serialized = objectMapper.writeValueAsString(foo);
JsonObject object = parser.parseObject(serialized);
assertThat(object.getString("attribute")).isEqualTo("Hi wrld");
assertThat(object.getInt("anotherAttribute")).isEqualTo(42);
assertThat(object.getObject("nestedJsonj")).isEqualTo(nested);

// or of course

Foo fooParsed = objectMapper.readValue(serialized, Foo.class);
// etc.
```

## Serializing

```
JsonObject o=parser.parse(inputStream);
// Jsonj objects know how to serialize themselves
String oneLine=o.toString();
// pretty printing is nice when reading json
String pretty=o.prettyPrint();
// you can also serialize straight to a writer of course.
o.serialize(writer);
```

## Json manipulation

```
// easy object and array creation using simple static factory methods, varargs, and smart polymorph argument processing
JsonObject o = object(
  field("hello","world"),
  field("another_field",array("of","mixed","types",42, 42.0,true,false,null)),
  field("nested",object(field("nested_field",42)))
);

// basic element lookup
JsonElement e = o.get("hello"); // JsonPrimitive with value "world"
JsonElement e2 = o.get("nested"); // JsonObject
JsonElement e3 = o.get("nested","nested_field"); // JsonPrimitive with value 42

// type safe value extraction
Integer value = o.getInt("nested","nested_field"); // 42
Integer noValue = o.getInt("nested","idontexist"); // null
Integer defaultValue = o.getInt("idontexist", 42); // null
Optional<Integer> o.maybeGetInt("nested","idontexist"); // Optional.empty();

// there are several variations of these for strings, booleans, longs, doubles, floats, and Number.

// convert JsonElements
JsonElement e= ....
int val=e.asInt(); // throws JsonTypeMismatchException if the element is not a JsonPrimitive or cannot be converted to an int.
JsonObject object = e.asObject(); // throws JsonTypeMismatchException if e.isObject() == false
JsonArray object =  e.asArray(); // throws JsonTypeMismatchException if e.isArray() == false

// on the fly nested object and array creation
//creates a five level deep array at o.l1.l2.l3.l4.a1=[1,2,3]
o.getOrCreateObject("l1","l2","l3").getOrCreateArray("l4","a1").add(1,2,3);
// adds three more elements to the array
o.getOrCreateObject("l1","l2","l3").getOrCreateArray("l4","a1").add(4,5,6);

```

## Collections

```
// objects are Map<String,JsonElement>
// so this works, as well as all of the Map API in java 8.
object.forEach((key,element) -> {...}); // easy iteration over entries in the Map<String,JsonElement)

// likewise, JsonArray implements List<JsonElement>
// so you can do stream processing
JsonArray result = object.getOrCreateArray("array").stream().map(element -> element.asObject().getString("nested_field")).collect(JsonJCollectors.array());

// Java has sets and they are useful, JsonSet implements Set<Element>
// asSet converts the list to a set and removes duplicate elements
// inserts of duplicate elements replace the old value
JsonSet set = object.getOrCreateArray("array").asSet();
// by default JsonSet uses element equals for identity but you can change this
JsonSet set2 = object.getOrCreateArray("array")
   .asSet()
   // return true if left and right elements are equal, false otherwise);
   .applyIdStrategy((left,right) -> ...);

// or use the simple id field strategy for the common case where you
// have an array of objects with an id field
// compares objects on their id field, throws JsonTypeMismatchException
// if elements are not objects.
JsonSet set3 = object.getOrCreateArray("array").asSet().applyIdStrategy("id");
```

## Polymorphism and varargs

JsonJ is all about making life easy. So it uses varargs wherever it makes sense and polymorphism to make most type conversions unnecessary.

```

JsonArray array=array(); // factory method for new JsonArray();
// array() has several polymorph implementations that support varargs as well.
array = array("foo","bar") // creates a new array with two JsonPrimitives.

// JsonArray and JsonSet have several add methods
array.add(primitive(1)); // adds 1
array.add(1); // does the same and converts 1 to a JsonPrimitive for you
// varargs are nice.
array.add(2,3,4);
array.add(true) // adds a JsonPrimitive with value true
array.add("hello") // add a string primitive

JsonObject map=new JsonObject();
// these all do the same ...
map.put("field", primitive(42));
map.put("field",42);
map.put("field",new JsonPrimitive(42));

// Maps are lists of entries, so you can add entries.
// use the field factory method to create entries
// field is polymorph and tries to do the right thing, just like JsonArray add.
Entry<String,JsonElement> entry=field("field",42);
map.add(entry);
map.add(field("1",1),field("2",2)); // add supports varargs of course.

```
You can find the `array`, `set`, `field`, and `object` methods in the `JsonBuilder` if your IDE supports this (eclipse), you'll want to configure this as a favourite to add the static imports for this.

## Iterating over stuff

There are convenient methods to iterate over different types of elements in an array. This saves you from manual object casting/conversion.

ints(), longs(), strings(), arrays(), and objects() are supported for this.

```java
// [1,2,3]
for(int i: array(1,2,3).ints()) {
  ..
}
```
## Java 8 Streams

Or you can use the new Java 8 streams:

```java
// do somthing for each entry in the dictionary
object.forEach(k,v) -> {
...
}

// or stream over elemnts in a json Array
someArray.stream().forEach(element -> ...); // as inherited from ArrayList
someArray.streamInts().forEach(i -> ...); // same but maps elements to ints
someArray.streamObjects().forEach(object -> ...); // maps to JsonObject

// etc

```

```java
// extract the name fields of objects inside an array and collect them in a JsonSet
JsonSet names = array.streamObjects().map(o -> o.getString("name")).collect(JsonJCollectors.set());
```

Of course all this convenience means that sometimes things don't work. All the exceptions in jsonj are unchecked, so there is no need to litter your business logic with try .. catch.

```java
array("foo").ints() // throws JsonTypeMismatchException
array(1,2,3).asObject() // throws JsonTypeMismatchException
```

# More Reasons to use JsonJ

There are several reasons why you might like jsonj

- The provided JsonObject, JsonArray, JsonSet, and JsonPrimitive classes are all you need for type safe and null safe manipulation of complex json structures.
- It comes with convenient builder classes for quickly constructing complex json datastructures without going through the trouble of having to create model classes for your particular flavor of json, piecing together lists, maps, and other types and then serializing those, or generally having to do a lot of type casts, null checks, generics juggling, etc.
- Memory efficient: you can squeeze large amounts of json objects in a modest amount of RAM. This is nice if you are doing big data processing projects.
- Easy to use and lacks the complexity of other solutions. All you do is JsonObject o = parseObject(...) and o.toString() or o.serialize(..).
- There are no annotations or model classes. This makes jsonj great for quickly prototyping some logic around any bit of json encountered.
- You can use the Jackson ObjectMapper with jsonj and have a `class Foo {JsonObject someNestedField; int bar; String foo}` mapped as you would want. This allows you to have hybrid schema less Jsonj and strongly typed pojos.
- A JsonDataObject interface is provided (that includes default methods) that allows you to easily create domain objects based on JsonObject. This interface provides a lot of default methods and acts a mixin. This gives you the best of both worlds and it is easy to reuse functionality between different domain classes.
- It relies on the excellent [Jackson](https://github.com/FasterXML/jackson-core) parser for parsing data structures and you might already use jackson.
- In addition to the popular json format it also supports parsing and serializing to *[Hocon](https://github.com/jclawson/jackson-dataformat-hocon), [BSON](https://github.com/michel-kraemer/bson4jackson), [plist](https://github.com/3breadt/dd-plist), and [YAML](https://github.com/FasterXML/jackson-dataformat-yaml)*. So you can deal with tree like data structures and pretend they are all the same. For hocon we currently don't have serialization. Mostly this support is done via jackson plugins. They all drive the same jackson handler in jsonj. So, barring downstream parsing issues; you get the same functionality with each of them. Also adding support for more jackson plugins is easy.
- [JsonLines](http://jsonlines.org/) support via `Stream<JsonObject> JsonParser.parseJsonLines(Reader r)`

There are probably more reasons you can find to like JsonJ, why not give it a try? Let me know if you like it (or not). Let me know it should be changed in some way.

## Memory efficient

JsonJ implements several things that ensure it uses much less memory than might otherwise be the case:

- it has several `JsonObject` implementations that have different memory and performance characteristics. You can control the behavior from the parser settings and mostly you don't need to worry about which is used.
- `JsonObject`. This is the default implementation. It uses a custom `Map` implementation with two arrays for the keys and values. This ensures insertion order is reflected when iterating and performs well for small objects.
- SimpleIntMapJsonObject uses my EfficientString library for object keys. This means key instances are reused and stored as UTF8. Assuming you have millions of objects that use a handful of keys like 'id', 'name', etc., You only store those byte arrays once and the objects refer these by an integer id. Don't use this implementation if you expect millions of different keys because you may run out of memory.
- `MapBasedJsonObject`. Because neither `JsonObject` nor `SimpleIntMapJsonObject` scale to large numbers of keys, the parser switches to this implementation automatically when the number of keys exceeds the configurable threshold (default is 100). This implementation uses a simple LinkedHashMap instead. This uses way more memory but scales to much larger amounts of keys than either of the other implementations.
- Both `SimpleIntMapJsonObject` and `JsonObject` use UTF8 byte arrays for storing String primitive values. This is more efficient than Java's own String class, which uses utf-16.

# Changelog
- 2.51
  - Add support for kotlin enum values in the JsonObject.fill and JsonObject.construct extension methods for Kotlin
- 2.50
  - Get rid of maven in favor of gradle, make project ready for Kotlin
  - Add some kotlin specific code and extensions
  - Make JsonDataObject work with jackson ObjectMapper so you can seemlessly start migrating to hybrid jsonj/pojos.
- 2.45
  - Simple TOML support to convert toml to JsonObject and back.
- 2.44
  - Test release to verify my pgp key for sonatype is still ok (new laptop). Effectively the same as the previous release.
- 2.43
  - objectmapper related tests and minor features
- 2.42
  - Make sure deepClone and immutableClone use the same JsonObject class as the object being cloned. This solves some performance issues where it regresses to using JsonObject instead of MapBasedJsonObject when cloning a big instance.
- 2.41
  - Smarter handling of numeric strings. You can now use `asInt()`, `asDouble()`, etc on quoted numeric values without failing with a type exception.
- 2.40
  - Proxy the new maybeGet* methods added in 2.34 to JsonDataObject as well.
- 2.39
  - Align put behavior of JsonDataObject with that of JsonObject. You can now put any object instead of just primitives. Fixes a bug where putting another JsonDataObject resulted in the string serialization of the object being added as a primitive instead of the object.
- 2.38
  - Introduce shared interface with default methods for the three JsonObject implementations and strip the classes of duplicated implementations. This should be fully backward compatible.
- 2.37
  - Introduce a new JsonObject implementation that does not rely on EfficientString.
  - Introduce optional settings for JsonParser to control what implementation is used. You can switch back to the SimpleIntMapJsonObject that was the default before this release.
- 2.36
  - JsonBuilder, JsonObject, and JsonArray now know how to handle Optionals. This solves a minor issue where Optional instances end up being handled as primitive strings (toString gets called on the Optional). Now it does the right thing and gets the value or if not there it uses a null primitive.
- 2.35
  - Minor improvement to recently added json lines support in the parser. Now we ignore empty lines and comment lines starting with `#`.
- 2.34
  - Add maybeGet, maybeGetString, maybeGetObject, etc. accessors that return an `Optional`. You can use this as an alternative to the existing accessors that may return null.
- 2.33
  - Add parseJsonLines() to JsonParser to allow easy processing of jsonlines.org style input. Returns a `Stream<JsonObject>` that you can process without having to buffer everything in memory.
- 2.30 - 2.32
  - Add @Nonnull annotations in a few places
  - flatten method in jsonobject
- 2.29
  - Small performance enhancement to json set with id strategy
  2.28
  - Improve number handling. We now instantiate BigDecimal or BigInteger if the parsed number text length goes over a threshold. This means we no longer get E notation for longer decimal numbers. We still use Long or Double when this is possible so we don't waste memory unless it is necessary.
- 2.27
  - [Hocon support](https://github.com/jclawson/jackson-dataformat-hocon) added.
  - update all outdated dependencies (jackson and a few others).
- 2.26
  - Fix broken MapBasedJsonObject parser support in 2.25 to actually work as intended; simplify the handler code to have less unnecessary conditional logic
- 2.25
  - Add support for parsing large json objects with many keys by falling back to a new `MapBasedJsonObject` that can be configured to use any kind of `Map`. By default this uses a `LinkedHashMap` which comfortably handles millions of keys (at the expense of using more memory). The parser handler now automatically converts objects over 100 keys to this and there is a new `toMapBasedJsonObject()` method that can convert existing JsonObjects (by cloning them). WARNING: this release should be considered broken. Use 2.26.
- 2.24
  - Generalize fromObject to handle any Collection as a JsonArray instead of just List
- 2.23
  - withIdStrategy on json set now supports multiple fields via varargs
- 2.21
  - fix polymorphism bug with put of JsonDataObject being serialized to String on object put.
- 2.20
  - Relax isEmpty() on JsonObject because this ended up being to aggressive when recursing on lists.
- 2.19
  - Add BSON support through the optional dependency bson4jackson (note: experimental; report bugs if you find any).
  - Add some asserts to JsonObjectAssert
- 2.18
  - Fix serialization issue with very large json objects when pretty printing is turned on
- 2.17
  - update dependencies and move to commons-lang3
- 2.16
  - Support jackson parser features in JsonParser
  - Fix API for findFirstWithFieldValue to return an Optional and return the element instead of a copy of the element.
- 2.15 add some syntactic sugar for finding objects inside arrays and sets using findFirstWithFieldValue, findFirstMatching, or filter
- 2.14
  - Add mapPrimitiveFieldsRecursively function to JsonObject so you can easily do some search and replace on json objects; even if they contain lists of objects
  - fix the old JsonObject.map() to actually work in a sensible way.
  - add forEachPrimitiveRecursive for each to JsonObject
- 2.12 Improve JsonDataObject handling on object put and set contains
- 2.11 Have more sensible way to set the id strategy on json sets that does not create a new set and modifies the current one instead. This bit me several times and once too many.
- 2.10 Allow use of JsonDataObject in all the obvious places to easily add them to sets, objects, and arrays.
- 2.9 Add isMutable() method to JsonDataObject and JsonElement interfaces. Support remove() and containsKey() on JsonDataObject.
- 2.8 Add JsonDataObject interface to support creating domain classes based on JsonObject.
- 2.7 Update jackson dependency.
- 2.6 Add support for yaml using jackson's jackson-dataformat-yaml. YamlParser and YamlSerializer parse and serialize to and from JsonJ.
- 2.5 Add support for parsing and serializing plists. Add custom [assertj](http://joel-costigliola.github.io/assertj/) assertions.
- 2.4 asNumber method added to JsonElement
- 2.3 Fix issue with emoji. These unicode characters were being dropped because they are outside the XML supported ranges of unicode characters. Now they are added in escaped form.
- 2.2 Add immutableClone method to JsonElement that allows you to get an immutable json element.
- 2.1 Improve memory footprint of JsonObject by refactoring the SimpleMap to use an int array to store EffecientString references instead of object references.
- 2.0 New release that requires Java 8
  - adds convenient integration with Java 8 specific features. Since this breaks compatibility somewhate, I've bumped the version to 2.0. The last 1.x release should be fine for usage and there is a 1.x branch in git as well. However, it is unlikely that I will support this branch in the future.
  - JsonjCollectors provides collectors for the stream API that can collect elements of any type into a JsonArray or JsonSet. The implementation uses fromObject to convert anything that is not a JsonElement.
  - streamObjects and streamStrings methods added to JsonArray that allows you to manipulate streams of JsonObject and streams of String.
  - forEachObject method added to JsonArray
  - forEachString added to JsonObject, useful for manipulating properties of String->String
  - Serialize now takes a writer instead of an outputstream. OutputStreams are still supported but you should be using writers everywhere.
- 1.51 Fix minor issue with parsing empty string. Now throws a JsonParseException if you try this.
- 1.50 add missing parseObject and parseArray methods to JsonParser. These methods disappeared because of the change in the last release.
- 1.49 switch parser backend to jackson and remove dependency on json-simple. Reason for this is that I stumbled upon a bit of invalid json that was actually parsing successfully with json simple. The jackson parser fails as expected. This should not impact anyone since this is an internal change and the API stays the same.
- 1.48 add and remove now replace and remove using the id strategy instead of object equals in JsonSet.
- 1.47
  - make array and set remove a bit smarter. Now handles primitive values as you would expect.
  - add custom id strategy to JsonSet. This allows you to override the equals behavior of JsonElements in the set and influence the behavior of the add and contains methods.
- 1.46 add getOrCreateSet method to JsonObject
- 1.45 array and set now accept any iterable and do the right thing when the Iterable is a JsonElement. In that case the element is added instead of behaving like an addAll. Both methods now also accept java arrays of various types. If you used array(array(1,2,3)) and were relying on the addAll behavior, things are going to break for you.
- 1.44 Add a few convenient methods to JsonSet and JsonArray to allow for conversion between the two and easy element replacement.
- 1.43 Add convenience methods on JsonObject for getting primitives with default values; fix add method on JsonArray to not convert JsonElement to JsonPrimitive.
- 1.41 Remove $ and _ methods from API since these conflict with Java 8 code guidelines as enforced by the more strict javadoc default settings. This breaks the builder API unfortunately but I believe it had to be done. Fortunately the fix is easy: simply use the object, field, and array methods.
- 1.40 JsonArray was now has convenient add method for numbers and booleans in addition to the existing `add(String...s)` method.
- 1.39 Parser can has a convenient `parseObject` method
- 1.38 add geeky feature to generate java code to drive the `JsonBuilder` from the actual `JsonElement`. Useful to convert json fragments into code (e.g. a complex elastic search query).
- 1.37 Several fixes for escaping. It turns out we had two code paths for escaping and they weren't doing the same things. Now it uses the same codepath always. This mostly only affects edgecases where the json contains weird control characters that probably shouldn't be there to begin with.
- 1.35 Filter out characters that are not allowed in XML as well. This should fix some weird parsing issues I'm seeing with Jackson.
- 1.34 Filter out iso control codes during serialization.
- 1.33 JsonJ now deployed in Maven Central again.
- 1.30 Fix for incorrect shape type when using pointShape in `GeoJsonSupport`
- 1.29 Fix for efficient string race condition
- 1.27-1.28 Hopefully fix race condition with efficient string once and for all.
- 1.26 Support `$` as an alias for `array()` as well. Yay polymorphism! `JsonObject o=$(_("foo","bar"), _("list", $(1,2,3)))`
- 1.25 Create `$` and `_` aliases for `object` and `field`: `JsonObject o=$(_("foo","bar"), _("list", array(1,2,3)))` now works as well.
- 1.24 Support new style of creating JsonObjects: `JsonObject o=object(field("foo","bar"), field("list", array(1,2,3)))` now works. You can also add multiple field entries to an existing object or take the entrySet of an existing object and add those entries as fields to an object.
- 1.23
    - Add not null validation for object keys; prevents npe's
- 1.22
    - Add arrays, strings, longs, doubles iterator methods to `JsonArray` so you can foreach over elements of that type without any conversions. We already had objects(). for example:
    ```java
        for(String s: jsonArray.strings()) {
            System.out.println(s);
        }
    ```
- 1.21
    - Allow `null` values to be added as json null instead of rejecting them with an illegal argument exception.
- 1.20
    - handle json nulls when getting java boxed values and return a java null instead of throwing an npe
- 1.19
    - add asFloat, getFloat, and getLong methods
- 1.18
    - add asLong method for when asInt is too short
- 1.17
    - update efficientstring
- 1.16
    - support addition of JsonBuilder objects in arrays, sets, and objects )
    - support JsonSet in builder
- 1.15
    - add convenient objects iterable to JsonArray to allow iterating over JsonObjects without calling asObject on each JsonElement. Works for JsonSet as well.
- 1.14
    - Fix resource leak with jackson parser not being closed; make jackson not optional to fix classpath issues
- 1.13
    - Use new SimpleMap instead of HashMap in JsonObject. This is faster and more memory efficient for small amounts of keys
    - JsonArray now extends ArrayList instead of LinkedList
    - Add new parser based on jackson. Both parsers use the same handler so there is not much difference in performance.
- 1.12
    - Fix GeoJson type bugs
- 1.11
    - Add simple JsonSet implementation because sometimes it is just nice to have lists that behave like sets. This is a simple variation of the JsonArray that is not supported during parsing.
    - Fix bug with primitive(primitive(42)) ending up creating a primitive for the quoted value.
- 1.10
    - require java 1.7
    - move jsonj.rb, see my jsonj-integration project or install the gem from rubygems
    - ensure remove empty does not remove empty nested objects
    - make parseResource fall back to file input stream
    - use fixed version of efficientstring that is threadsafe
- 1.7 (and 1.5, 1.6)
    - misc API cleanup
    - no more key interning
    - adapted pretty printing
    - fix jruby integration to work again with the utf-8 related changes
- 1.4
    - Fix two bugs with the containsKey and serialization
- 1.3
    - Promote asInt,asDouble,asBoolean from JsonPrimitive to JsonElement for convenience.
- 1.2 - Use utf8 byte arrays to conserve memory
    - String values are now represented as UTF8 byte arrays rather than 16 bit characters
    - EfficientString is used for JsonObject keys.
    - JsonElement now has a new serialize method that serializes straight to an OutputStream. JsonSerializer now has a new serialize method to utilize this.
    - The String serialize(..) method now uses the new efficient serialization if pretty printing is turned off
- 1.1 - minor feature added
    - Added first and last convenience methods to JsonAray
- 1.0 - only one change relative to 0.9
    - Fixed JsonArray.equals to be more strict
- 0.9 - big release with many new features that resulted from months of using jsonj in my own project
    - fixes several serialization bugs; serialization and parsing is now very robust
    - array.addAll and the builder now play nice with collections
    - toString now serializes proper json on json strings: they now include quotes!; use asString instead).
    - added prettyPrint method to JsonElement that returns a pretty printed string representation of the element (uses JsonSerializer)
    - added a fromObject method to the builder that handles nested maps and lists and converts those to json
    - added methods to JsonArray to convert to native double, int, or string arrays
    - added asString, asDouble, asInt methods to JsonElement so that you don’t have to chain asPrimitive.asString anymore.
    - added a jsonj.rb script that integrates JsonJ into jruby and allows you to convert back and forth between JsonElement and jruby’s Hash and Array native types.
- 0.8 fix escaping, npe fix John Goodwin merged, misc minor fixes
- 0.7 Add support for converting a json element to a dom tree so that things like xpath or xsl can be used on it
- 0.6 Support for deepCloning, object sorting added. Use String.intern() on object keys to optimize frequent operations on json objects.
- 0.5 Serializer and parser now properly escape and unescape string literals.
- 0.4 Bug fixes, changed groupid, documentation.
- 0.3 Several new methods added in JsonArray and JsonObject; fixed the parser bug where nested arrays and objects were not handled correctly.
- 0.2 Several bug fixes
- 0.1 First release
