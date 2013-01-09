# you need jbundler or some other means of getting the jsonj jar on the classpath
require 'jbundler'

java_import com.github.jsonj.JsonObject
java_import com.github.jsonj.JsonArray
java_import com.github.jsonj.JsonPrimitive
java_import com.github.jsonj.tools.JsonBuilder

def convertToRuby(value)
  if value.isObject
    result = Hash.new
    value.each{|k, v| result[k] = convertToRuby(v)}
    result
  elsif value.isArray
    result = Array.new
    value.each{|v| result << convertToRuby(v)}
    result
  elsif value.isPrimitive
    value.value
  end
end

JsonObject.send(:define_method, 'toRuby') do 
  convertToRuby self
end

JsonArray.send(:define_method, 'toRuby') do 
  convertToRuby self
end

JsonPrimitive.send(:define_method, 'toRuby') do 
  self.value
end

Hash.send(:define_method, 'toJsonJ') do
  JsonBuilder.fromObject(self)
end

Array.send(:define_method, 'toJsonJ') do
  JsonBuilder.fromObject(self)
end
