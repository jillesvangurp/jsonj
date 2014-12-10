# Introduction

JsonJ is a library for working with json in Java without mappings or model classes. Instead JsonJ backs the very simple json types using a fluid API that extends the relevant Java Collections APIs where possible (Map, List, Set).

There are several reasons why you might like jsonj

- it provides really convenient builder classes for quickly constructing complex json datastructures without going through the trouble of having to create model classes for your particular flavor of json, piecing together lists, maps, and other types and then serializing those, or generally having to do a lot of type casts, null checks, generics juggling, etc.
- it provides powerful extensions to the Collections API that, for example, makes extracting things from lists and maps a lot easier.
- it is memory efficient: you can squeeze millions of json objects in a modest amount of RAM. This is nice if you are doing big data processing projects. If you've ever had to worry about fitting gigantic amounts of structured data in memory, you might appreciate some of these optimizations.
- it's simple to use and lacks the complexity of other solutions. There are no annotations. There is no need for model classes.
- it uses the excellent jackson parser for parsing data structures and you might already use jackson.
- In addition to the popular json format it also supports parsing and serializing to binary plist and YAML. 

There are probably more reasons you can find to like JsonJ, why not give it a try? Let me know if you like it (or not). Let me know it should be changed in some way.

# Get it from Maven Central

```
<dependency>
    <groupId>com.jillesvangurp</groupId>
    <artifactId>jsonj</artifactId>
    <version>2.5</version>
</dependency>
```

Note. always check for the latest version. I do not always update the readme.

For Java 7 and earlier, use the 1.x releases. 2.x is Java 8 only because of the integration with the new Streams API in java 8 as well as some other new language features that are used.

# Features

The purpose of the JsonJ framework is to allow you to write code that manipulates json data structures that has a low amount of verbosity compared to other frameworks.

The JsonJ API has been finetuned over several years of using it for real work. I have done my best to eliminate the need for any code that feels like it is overly repetitive or verbose (aka the *DRY principle*). Any time I write code using JsonJ that feels like I'm repeating myself, I fix it.

I regard this as the key selling point of the library. When manipulating and creating json structures programmatically, it is important that you don't have to jump through hoops to extract elements, iterate over things, etc. To facilitate this, the framework provides a large amount of convenient methods that help *prevent verbosity* in the form of unnecessary class casts, null checks, type conversions, generic types, etc. No other Json framework for Java comes close to the level of usability of this framework when it comes to this. Most leave you to either develop your own classes or with the bare bones API of the Java collections framework.

## Design overview and API

JsonJ provides a few easy to understand classes that extend Java’s Collections framework. Extending the Collections framework means using a familiar, and powerful API that most Java coders already know how to use. The following classes are provided:

- `public class JsonObject implements Map<String,JsonElement>, JsonElement`
- `public class JsonArray extends ArrayList<JsonElement> implements JsonElement`
- `public class JsonSet extends JsonArray` (because sometimes having duplicate free lists is a nice thing)
- `public class JsonPrimitive implements JsonElement`

As the class signatures suggest, these classes provide a type safe alternative to simply using generic maps/lists of Objects since everything implements JsonElement.

The `JsonElement` interface specifies a lot of convenience methods that allow you to do easy type checks and to convert to/from Java native type (when needed), etc. For example `String s = e.asArray().last().asObject().getArray("key").get(3).asString()` actually digs out a string from a list inside an object that is the last element in another list without requiring type casts. The as- methods convert to common types or throw an unchecked exception if the conversion is impossible and there are also is- methods for checking the type conditionally. This gets rid of a lot of type checks, type casts, and other ugly code.

Additionally a lot of methods are polymorphic and accept different types of objects (unlike the methods in the Collections framework). For example, the `add` method on JsonArray is polymorphic and automatically generates primitives if you add Strings, Booleans, or Numbers. The `put` on JsonObject behaves the same. The `add` method support varargs, so you can add multiple elements in one call. 

## JsonBuilder

To facilitate creation of json objects, arrays, and primitives a builder class is included that makes creation of nested json object structures as easy as it gets in Java. It is recommended to **use static imports and to add this class as a favorite in eclipse to facilitate autocompletion**. JsonBuilder is a one stop shop for constructing very complex json objects effortlessly.

### Factory methods without builders

JsonBuilder started out as an ordinary builder and you can still use it as such. However, I find factory methods are a much cleaner way of constructing objects, arrays, and sets.

```java
import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.field;
import static com.github.jsonj.tools.JsonBuilder.object;

...

JsonObject o=object(
    field("aList",array(
        1,
        2,
        object(field("meaningoflife",42)),
        "no more builder"))
    ),
    field("another", "element"),
    field("aSet",set(1,2,3),
    field("nestedlists",array(
       array(1,2),
       array(3,4)
     ))
);
```

The `object` methods supports a varargs element of the type `Map.Entry`, which is the type you normally get when iterating over a Map entryset. To support creating those, there is the `field` method, which returns a `Entry<String,JsonElement>` instance that you can simply `add` to the `JsonObject` as well. 

Notice how you can mix integers, strings, objects in a typesafe way. They are all converted for you to JsonElement using the fromObject method in JsonBuilder which converts objects in the most appropriate JsonJ equivalent. So Booleans, Integers, Doubles, Strings, etc. all become JsonPrimitives. Any JsonElement implementations are used as is and `Map` or `List` implementations get converted to JsonObject and JsonArray instances.

Finally, the `array` method constructs a JsonArray using its varargs elements. Naturally it supports the same behavior of doing the right thing with any kind of object. You can even make it 

### Classic builder pattern

This works by chaining method calls that all return a `JsonBuilder` and then using the `get()` method to get to the constructed object.

```java
import static com.github.jsonj.tools.JsonBuilder.array;
import static com.github.jsonj.tools.JsonBuilder.object;
...

JsonObject o=object()
  .put("aList",array(
    "lets start with a Json list of mixed Json types, all type safe and Generic",
    1,
    2,
    object()
      .put("meaningoflife",42),
      "note that the nested builder’s get() method is called for you; it understands what to do with the builder")
    )
  )
 .put("here", "is a simple string field")
 .put("aSet",set("a set does not exist in json; it has only arrays","but sometimes it is useful to have sets", "in JsonJ a set is just a simple variation of a list", "and of course it implements Set<JsonElement>")
 .put("nestedlists",array(
    array(1,2),
    array(3,4)
  ))
 .get();
```

### Misc. other builder features

The builder class also provides methods to facilitate converting from existing Maps, Lists, and other objects. For example, the fromObject method takes any Java object and tries to do the right thing.

## Manipulating Json Programmatically

The default add and put methods in List and Map are polymorph in JsonArray, JsonSet, and JsonObject. So, they understand how to do the right thing for different types.

```java
JsonArray a = array();
a.add(primitive(1));
a.add(1);
a.add("1");
a.add(1.0);

JsonObject o = new JsonObject();
// these four lines do the same thing
o.put("field", new JsonPrimitive(42)));
o.put("field", primitive(42));
o.put("field", 42);
o.add(field("field",42));
// {"field":42}

```

You can easily create and manipulate nested objects or arrays with getOrCreateObject or getOrCreateArray. Both methods only work on objects and save you from having to recursively add objects and check for their existence while you do so.

```java
JsonObject object = new JsonObject()
// -> {}
object.getOrCreateObject("f","o","o").add(field("foo","bar"));
// -> {"f":{"o":{"o":{"foo":"bar"}}}}
object.getOrCreateArray("b","a","r").add(1,2,3);
// -> {"f":{"o":{"o":{"foo":"bar"}}},"b":{"a":{"r":[1,2,3]}}}
```


There are convenient methods to iterate over different types of elements in an array. This saves you from manual object casting/conversion.

ints(), longs(), strings(), arrays(), and objects() are supported for this.

```java
// [1,2,3]
for(int i: array(1,2,3).ints()) {
  ..
}
```
Or you can use the new Java 8 streams:

```java
// do somthing for each entry in the dictionary
object.forEach(k,v) -> {
...
}
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

## Parsing and serialization

- A thread safe `JsonParser` class is provided that uses jackson's streaming parser.
- You can serialize using `toString()` or `prettyPrint()` or `serialize()` on any JsonElement, or you can use the `JsonSerializer` class directly.
- There's a YamlParser, YamlSerializer, JsonjPlistParser, and JsonjPlistSerializer as well.

## Java 8

Java 8 added lambda functions and the streaming API. While 1.x already works fine with this due to the fact that JsonJ fully supports the Collections framework. JsonJ 2.x adds several convenient  methods that make this more user friendly. You can call streamObjects on arrays, which allows you to process json arrays of json objects. There is a new JsonjCollectors class that provides collectors for JsonArray and JsonSet that are capable of collecting Objects of any type supported by fromObject into JsonSet or JsonArray. Likewise both these classes have a new constructor that takes a stream.

## Unit testing using Assertj

As of 2.5, I've started adding support for custom jsonj assertion classes to support unit testing using assertj. This is a work in progress and I will be adding functionality here as I need it. This simplifies unit testing code that handles or returns jsonj instances. Note, as of 2.5 a lot of the unit tests are still using hamcrest as I have not converted the tests yet. I welcome pull requests for this :-).

## JRuby integration

If you use jruby, you can seemlessly integrate jsonj using [jsonj-integration](https://github.com/jillesvangurp/jsonj-integration). This module uses monkey patching to add various methods to classes that allow you to convert between ruby style lists and hashes and JsonJ classes. Additionally, it adds [] and []= accessors to JsonArray, JsonSet, and JsonObject, which allows you to pretend it is all ruby. Finally, it adds `to_json` and `to_s` methods that do the right thing in Ruby as well. I use this module to mix Java and Ruby in one project and this comes in quite handy.

## Memory efficient

JsonJ implements several things that ensure it uses much less memory than might otherwise be the case:

- it uses my EfficientString library for object keys. This means instances are reused and stored as UTF8. Assuming you have millions of objects that use a handful of keys like 'id', 'name', etc., You only store those byte arrays once.
- it uses UTF8 byte arrays for storing String primitives. This is more efficient than Java's own String class, which uses utf-16.
- it uses a custom Map implementation that uses two ArrayLists. This uses a lot less memory than e.g. a LinkedHashMap. The downside is that key lookup is slower for objects with large amounts of keys. For small amounts it is actually somewhat faster. Generally, Json objects only have a handful of keys thus this is mostly a fair tradeoff that saves a lot of memory.

## Odd features you probably don't care about

- `JsonElement` implements `Serializable` so you can serialize jsonj objects using Java’s builtin serialization, if you really want to use that (hint, you shouldn’t).
- A utility class is included that allows you to convert json to and from XML, and to create DOM trees from json object structures. This can come in handy if you want to use e.g. xpath to query your json structures. You need to add the optional dependency on xom for this to work.
- You can serialize to and parse from binary plists. This was added to support some IOS specific usecases. You need to add the optional maven dependency on dd-plist for this to work.
- Yaml is supported with its own parser and serializer, both based on Jackson's jackson-dataformat-yaml.

# Changelog
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


# FAQ

## What’s with the name

It’s pronounced json-j, or jasonjay. It doesn’t mean anything in particular other than”json for java“, or something. Well, trying to come up with a name that is not already used is quite a challenge and I wanted to stuff the acronym json in there, keep it short, and not have the first hit on Google be something else than this. So, JsonJ it is.

## For whom is this framework intended

Anyone who plans to write a lot of business logic in Java that manipulates json data structures and who doesn’t wish to write model classes in Java to hide the fact that json is being used. If you are like me, you feel somewhat stuck having to deal with awkward json frameworks while all the cool Ruby,Python, and Javascript kids get to use a serialization that is natively supported in their language. This framework is for you.

## Will there be a Java 8 version of JsonJ with closures?

Version 2 is Java 8 specific and provides integration with the new Streams api.

## I found a bug, what should I do

File a bug on this github project, or just mail/im me directly. Either way, if I agree something is broken, I will fix it. Alternatively, feel free to clone & own. That’s what github is all about.

## How to build JsonJ

`mvn clean install` should do the trick with maven 3.x (and probably 2.x as well).

## Where is the documentation?

Javadoc is generated during the build. After building you can find it in `./target/apidocs/index.html`
Additionally, look at the tests. Particularly [this one here](src/test/java/com/github/jsonj/ShowOffTheFrameworkTest.java) shows off most of the features this framework has.

# License

The license is the [MIT license](http://en.wikipedia.org/wiki/MIT_License), a.k.a. the expat license. The rationale for choosing this license is that I want to maximize your freedom to do whatever you want with the code while limiting my liability for the damage this code could potentially do in your hands. I do appreciate attribution but not enough to require it in the license (beyond the obligatory copyright notice).

# Acknowledgements

1.  I’ve been greatly influenced by the classes representing the json primitives in the GSon framework. If only they implemented Map and List and weren’t final. But lovely framework and would use it again.
2.  This code is very loosely based on work I did at work with several colleagues some years ago. No code was copy pasted but I definitely took some ideas and improved on them. You know who you are. Thanks.
