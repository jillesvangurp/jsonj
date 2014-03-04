package com.github.jsonj.tools;

import java.io.IOException;
import java.util.LinkedList;

import org.json.simple.parser.ContentHandler;
import org.json.simple.parser.ParseException;

import com.github.jsonj.JsonArray;
import com.github.jsonj.JsonElement;
import com.github.jsonj.JsonObject;
import com.github.jsonj.JsonPrimitive;

final class JsonHandler implements ContentHandler {
	// use a simple stack mechanism to reconstruct the tree
	LinkedList<JsonElement> stack = new LinkedList<>();
	boolean isObject = false;

	public JsonElement get() {
		// the remaining element on the stack is the fully parsed
		// JsonElement
		return stack.getLast();
	}

	@Override
	public boolean startObjectEntry(final String entry)
	throws ParseException, IOException {
		stack.add(JsonBuilder.primitive(entry));
		return true;
	}

	@Override
	public boolean startObject() throws ParseException, IOException {
		isObject = true;
		stack.add(new JsonObject());
		return true;
	}

	@Override
	public void startJSON() throws ParseException, IOException {
		// clean up from previous runs
		stack.clear();
	}

	@Override
	public boolean startArray() throws ParseException, IOException {
		isObject = false;
		stack.add(new JsonArray());
		return true;
	}

	@Override
	public boolean primitive(final Object object) throws ParseException, IOException {
		JsonPrimitive primitive;
		primitive = new JsonPrimitive(object);
		if (isObject) {
			stack.add(primitive);
		} else {
			JsonElement peekLast = stack.peekLast();
			if (peekLast instanceof JsonArray) {
				peekLast.asArray().add(primitive);
			} else {
				stack.add(primitive);
			}
		}
		return true;
	}

	@Override
	public boolean endObjectEntry() throws ParseException, IOException {
		JsonElement value = stack.pollLast();
		JsonElement e = stack.peekLast();
		if(e.isPrimitive()) {
			e=stack.pollLast();
			JsonElement last = stack.peekLast();
			if(last.isObject()) {
				JsonObject container = last.asObject();
				String key = e.asPrimitive().asString();
				container.put(key, value);
			} else if(last.isArray()) {
				throw new IllegalStateException("shouldn't happen");

			}
		}
		return true;
	}

	@Override
	public boolean endObject() throws ParseException, IOException {
		if(stack.size()>1 && stack.get(stack.size()-2).isArray()) {
			JsonElement object = stack.pollLast();
			stack.peekLast().asArray().add(object);
		}
		return true;
	}

	@Override
	public void endJSON() throws ParseException, IOException {
	}

	@Override
	public boolean endArray() throws ParseException, IOException {
		if(stack.size()>1 && stack.get(stack.size()-2).isArray()) {
			JsonElement value = stack.pollLast();
			stack.peekLast().asArray().add(value);
		}
		return true;
	}
}