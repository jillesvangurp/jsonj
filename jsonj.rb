#
# This module integrates jsonj into jruby by adding ruby functions to various classes.
# This allows you to use conversion methods on hashes, lists, and any JsonElement
# Additionally, you can use [] for getting, putting. and adding to JsonObject and JsonArray
# 
# This makes the integration nearly seemless and enables you to, for example, use JsonJ enabled 
# business logic written in Java from a jruby API layer (e.g. rails or sinatra based).
#

java_import com.github.jsonj.JsonObject
java_import com.github.jsonj.JsonArray
java_import com.github.jsonj.JsonPrimitive
java_import com.github.jsonj.JsonType
java_import com.github.jsonj.tools.JsonBuilder

# this function is injected into JsonElement subclasses
def convertToRuby(value)
  if value.isObject
    result = Hash.new
    
    value.entrySet.each{|e| 
      result[e.getKey] = convertToRuby(e.getValue)
    }
    result
  elsif value.isArray
    result = Array.new
    value.each{|v| result << convertToRuby(v)}
    result
  elsif value.isPrimitive
    if(value.type == JsonType::string)  
      
      value.asString
    else
      value.value
    end
  end
end

# java objects don't expose toString() via ruby's to_s(), so we fix this dynamically for JsonElement subclasses
def tos(caller)
  caller.toString
end

JsonObject.send(:define_method, 'to_s') do 
  tos self
end

JsonArray.send(:define_method, 'to_s') do 
  tos self
end

JsonPrimitive.send(:define_method, 'to_s') do 
  tos self
end

JsonObject.send(:define_method, 'toRuby') do 
  convertToRuby self
end

JsonObject.send(:define_method, '[]=') do |key,value|
  self.put(key.to_s,JsonBuilder::fromObject(value))
end


JsonObject.send(:define_method, '[]') do |key|
  val = self.get(key)
  if val.isPrimitive
    case val.type.to_s
    when 'string'
      val.asString
    when 'number'
      val.asDouble
    when 'bool'
      val.asBoolean
    when 'nullValue'
      nil
    end
  else
    val
  end
end

JsonArray.send(:define_method, '[]') do |key|
  val = self.get(key)
  if val.isPrimitive
    case val.type.to_s
    when 'string'
      val.asString
    when 'number'
      val.asDouble
    when 'bool'
      val.asBoolean
    when 'nullValue'
      nil
    end
  else
    val
  end
end

JsonArray.send(:define_method, '[]=') do |index,value|
  self.set(index,JsonBuilder::fromObject(value))
end

JsonArray.send(:define_method, '<<') do |value|
  self.add(JsonBuilder::fromObject(value))
end

JsonArray.send(:define_method, 'toRuby') do 
  convertToRuby self
end

JsonPrimitive.send(:define_method, 'toRuby') do 
  if(self.type.toString == 'string')
    self.asString
  else
    self.value
  end
end

# make sure jruby hashes know how to convert themselves to JsonObject
Hash.send(:define_method, 'toJsonJ') do
  JsonBuilder.fromObject(self)
end

# make sure jruby arrays know how to convert themselves to JsonArray
Array.send(:define_method, 'toJsonJ') do
  JsonBuilder.fromObject(self)
end
