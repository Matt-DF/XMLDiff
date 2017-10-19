package com.fabian.xmldiff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;



public class XMLDiff extends DefaultHandler {
	private StringBuffer buff = new StringBuffer();
	private Stack<Element> elements = new Stack<>();
	private List<Element> allElements = new ArrayList<>();
	
	public static void main(String[] args) throws Exception {
		final XMLDiff diff = new XMLDiff();
		
		if (args.length != 2) {
			System.out.println("Usage : XMLDiff filepath1 filepath2");
			System.exit(0);
		}
		
		final String file1 = args[0];
		final String file2 = args[1];
		
		System.out.print("Parsing file 1: " + file1 + " ");
		diff.run(file1);
		final Set<Element> one = new HashSet<Element>(diff.allElements);
		
		System.out.println(diff.allElements.size() + " Elements!");
		System.out.print("Parsing file 2: " + file2 + " ");
		diff.run(file2);
		System.out.println(diff.allElements.size() + " Elements!");
		
		final Set<Element> two = new HashSet<Element>(diff.allElements);
		final Set<Element> three = new HashSet<Element>(diff.allElements);
		
		System.out.println("Comparing nodes");
		// find all things present in two not present in one
		two.removeAll(one);
		
		System.out.println(two.size() + "  Missing from " + file1);
		for (Element el : two) {
			System.out.println(el.toString());
		}
		
		// remove all things present in one, not present in two.
		// since the removal of the two previously alters the set, we have a duplicate set of two - called three.
		one.removeAll(three);
		System.out.println("----------------------------------------");
		System.out.println(one.size() + "  Missing from " + file2);
		for (Element el : one) {
			System.out.println(el.toString());
		}	
	}

	private void run(String path) throws ParserConfigurationException, SAXException, IOException {
		final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		elements.clear();
		allElements.clear();
		parser.parse(path, this);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
	    
		final Element curr = new Element();
	    curr.name = qName;
	    for (int i = 0; i < attributes.getLength(); i++) {
	    	curr.attributes.put(attributes.getQName(i), attributes.getValue(i));
	    }
	    elements.push(curr);
	    allElements.add(curr);
	    buff.setLength(0);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
	    final Element el = elements.pop();
	    
	    el.ancestry = getAncestry();
	    
		el.text = buff.toString().replace("\n" , "").trim();
	    buff.setLength(0);
	}

	private String getAncestry() {
		final StringBuilder builder = new StringBuilder();
	    final Enumeration<Element> elementEnum = elements.elements();
	    
	    while (elementEnum.hasMoreElements()) {
	    	builder.append(elementEnum.nextElement().name);
	    	if (elementEnum.hasMoreElements()) {
	    		builder.append("->");
	    	}
	    }
		return builder.toString();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		buff.append(ch, start, length);
	}
	
	
	class Element {
		String ancestry;
		String name;
		String text;
	    Map<String, String> attributes = new HashMap<>();
		
	    @Override
		public int hashCode() {
	    	HashCodeBuilder builder = new HashCodeBuilder(17, 37)
	    			.append(name)
	    			.append(text)
	    			.append(ancestry);
	    			
	    	for (Map.Entry<String, String> entry : attributes.entrySet()) {
	    		builder.append(entry.getKey() + "_" + entry.getValue());
	    	}
	    		
	        return builder.toHashCode();
	    }
		@Override

		public boolean equals(Object obj) {
		   if (obj == this) return true;
		   
		   Element other = (Element) obj;
		   
		   if (!Objects.equals(other.name, this.name)) { return false;}
		   if (!Objects.equals(other.text, this.text)) { return false;}
		   if (!Objects.equals(other.ancestry,  this.ancestry)) {return false;}
		   
		   for (String key : this.attributes.keySet()) {
			   if (! Objects.equals(other.attributes.get(key), this.attributes.get(key))) {
				   System.out.println("Different attributes on " + this.name + "/" + other.name + " - " + key + ": " +  this.attributes.get(key) + " != " + other.attributes.get(key));
				   return false;
			   }
		   }
		   return true;
		}
		@Override
		public String toString() {
			return getString(0);
		}
		
		private String getString(int level) {
			StringBuilder sb = new StringBuilder();
			sb.append("Ancestry: ").append(ancestry)
			.append("\nName: ").append(name)
			.append("\nText: ").append(text)
			.append("\nAttributes: {");
			
			boolean first = true;
			for (Map.Entry<String, String> entry : this.attributes.entrySet()) {
				if (!first) {
					sb.append(", ");
				}
				sb.append(entry.getKey()).append(": ").append(entry.getValue());
			    first = false;
			}
			
			sb.append("}\n\n");
			
			
			return sb.toString();
			
		}
	    
	}
	
}
