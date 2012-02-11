package com.lloyd.JavaToProto;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Copyright - Lloyd Sparkes 2012
 * LICENSE: Public Domain - do as you wish, just retain this message.
 * 				I just ask that if you find bugs or improve the code, you raise a push request or an issue, so i can update the code for everyone
 * DISCLAIMER: I am not responsible for your usage of this code, or for any bugs or issues with this code or any resulting side effects
 * 
 * This class simply takes a POJO and creates the associated Proto Buffer Specification File.
 *  
 * Supports:
 * 		Nested POJO's
 * 		Enums
 * 		Arrays/Collections/Lists/Sets (BUT only if they have a type specifier!! (so List<Byte> is supported, List is not supported)
 * 		Maps/KeyValuePairs (BUT they need a type specifier!! (so Map<String,Integer> is supported, Map is not supported)
 * 		Primitives
 * 		Boxed Primitives 
 * 
 * Does Not Support:
 * 		Nested Collections e.g. Map<List<String>,String>
 * 		Arrays with more than one Dimension
 * 
 * Usage - CLI:
 * 		java -jar JavaToProto.jar JavaToProto <class name> [<output file name>]
 * 
 * 		If output file name is not specified the output will be to the console.
 * 
 * 		Ensure that the class name is in the class path somewhere.
 * 
 * Usage - Code:
 * 		
 * 		JavaToProto jpt = new JavaToProto(class name);
 * 		String protoFile = jpt.toString();	
 * 
 * @author Lloyd Sparkes
 */

public class JavaToProto {
	
	private static String NAME = "JavaToProto Generator";
	private static String VERSION = "v0.1";
	
	private static String OPEN_BLOCK = "{";
	private static String CLOSE_BLOCK = "}";
	private static String MESSAGE = "message";
	private static String ENUM = "enum";
	private static String NEWLINE = "\n";
	private static String TAB = "\t";	
	private static String COMMENT = "//";
	private static String SPACE = " ";
	private static String PATH_SEPERATOR = ".";
	private static String OPTIONAL = "optional";
	private static String REQUIRED = "required";
	private static String REPEATED = "repeated";
	private static String LINE_END = ";";
	
	private StringBuilder builder;
	private Stack<Class> classStack = new Stack<Class>();
	private Map<Class, String> typeMap = getPrimitivesMap();
	private int tabDepth = 0;

	/**
	 * Entry Point for the CLI Interface to this Program.
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length == 0){
			System.out.println("Usage: \n\tjava -jar JavaToProto.jar JavaToProto <class name> [<output file name>]\n");
		}
		
		Class clazz;
		
		try {
			clazz = Class.forName(args[0]);
		} catch (Exception e) {
			System.out.println("Could not load class. Make Sure it is in the classpath!!");
			e.printStackTrace();
			return;
		}
		
		JavaToProto jtp = new JavaToProto(clazz);
		
		String protoFile = jtp.toString();
		
		if(args.length == 2){
			//Write to File
			
			try{
				File f = new File(args[1]);
				FileWriter fw = new FileWriter(f);
				BufferedWriter out = new BufferedWriter(fw);
				out.write(protoFile);
				out.flush();
				out.close();
			} catch (Exception e) {
				System.out.println("Got Exception while Writing to File - See Console for File Contents");
				System.out.println(protoFile);
				e.printStackTrace();
			}
			
		} else {
			//Write to Console
			System.out.println(protoFile);
		}
		
	}
	
	/**
	 * Creates a new Instance of JavaToProto to process the given class
	 * @param classToProcess - The Class to be Processed - MUST NOT BE NULL!
	 */
	public JavaToProto(Class classToProcess){
		if(classToProcess == null){
			throw new RuntimeException("You gave me a null class to process. This cannot be done, please pass in an instance of Class");
		}
		classStack.push(classToProcess);
	}
	
	//region Helper Functions
	
	public String getTabs(){
		String res = "";
		
		for(int i = 0; i < tabDepth; i++){
			res = res + TAB;
		}
		
		return res;
	}
	
	public String getPath(){
		String path = "";
		
		Stack<Class> tStack = new Stack<Class>();
		
		while(!classStack.isEmpty()) {
			Class t = classStack.pop();
			if(path.length() == 0){
				path = t.getSimpleName();
			} else {
				path = t.getSimpleName() + PATH_SEPERATOR + path;
			}
			tStack.push(t);
		}
		
		while(!tStack.isEmpty()){
			classStack.push(tStack.pop());
		}
		
		return path;
	}
	
	public Class currentClass(){
		return classStack.peek();
	}
	
	public Map<Class,String> getPrimitivesMap(){
		Map<Class, String> results = new HashMap<Class, String>();
		
		results.put(double.class, "double");
		results.put(float.class, "float");
		results.put(int.class, "sint32");
		results.put(long.class, "sint64");
		results.put(boolean.class, "bool");
		results.put(Double.class, "double");
		results.put(Float.class, "float");
		results.put(Integer.class, "sint32");
		results.put(Long.class, "sint64");
		results.put(Boolean.class, "bool");
		results.put(String.class, "string");
		
		return results;
	}
	
	public void processField(String repeated, String type, String name, int index){
		builder.append(getTabs()).append(repeated).append(SPACE).append(type).append(SPACE).append(name).append(SPACE).append("=").append(SPACE).append(index).append(LINE_END).append(NEWLINE);
	}
	
	//end region
	
	private void generateProtoFile(){
		builder = new StringBuilder();
		
		//File Header
		builder.append(COMMENT).append("Generated by ").append(NAME).append(SPACE).append(VERSION).append(" @ ").append(new Date()).append(NEWLINE).append(NEWLINE);
		
		buildMessage();
		
	}
	
	private String buildMessage(){
		
		if(currentClass().isInterface() || currentClass().isEnum() || Modifier.isAbstract(currentClass().getModifiers())){
			throw new RuntimeException("A Message cannot be an Interface, Abstract OR an Enum");
		}
		
		String messageName = currentClass().getSimpleName();
		
		typeMap.put(currentClass(), getPath());
		
		builder.append(getTabs()).append(MESSAGE).append(SPACE).append(messageName).append(OPEN_BLOCK).append(NEWLINE);
		
		tabDepth++;
		
		processFields();
		
		tabDepth--;
		
		builder.append(getTabs()).append(CLOSE_BLOCK).append(NEWLINE);
		
		return messageName;		
	}
	
	private void processFields(){
		Field[] fields = currentClass().getDeclaredFields();
		
		int i = 0;
		
		for(Field f : fields){
			i++;
			
			int mod = f.getModifiers();
			if(Modifier.isAbstract(mod) || Modifier.isTransient(mod)){
				//Skip this field
				continue;
			}
			
			Class fieldType = f.getType();
			
			//Primitives or Types we have come across before
			if(typeMap.containsKey(fieldType)){
				processField(OPTIONAL,typeMap.get(fieldType), f.getName(), i);
				continue;
			}
			
			if(fieldType.isEnum()){
				processEnum(fieldType);
				processField(OPTIONAL,typeMap.get(fieldType), f.getName(), i);
				continue;
			}
			
			if(Map.class.isAssignableFrom(fieldType)){
				Class innerType = null;
				Class innerType2 = null;
				String entryName = "Entry_"+f.getName();
				
				Type t = f.getGenericType();
				
				if(t instanceof ParameterizedType){
					ParameterizedType tt = (ParameterizedType)t;
					innerType = (Class) tt.getActualTypeArguments()[0];
					innerType2 = (Class) tt.getActualTypeArguments()[1];	
					buildEntryType(entryName, innerType, innerType2);
				}
				
				processField(REPEATED,entryName, f.getName(), i);
				continue;
			}
			
			if(fieldType.isArray()){
				Class innerType = fieldType.getComponentType();
				if(!typeMap.containsKey(innerType)){
					buildNestedType(innerType);
				}
				processField(REPEATED,typeMap.get(fieldType), f.getName(), i);
				continue;
			}
			
			if(Collection.class.isAssignableFrom(fieldType)){
				Class innerType = null;
				
				Type t = f.getGenericType();
				
				if(t instanceof ParameterizedType){
					ParameterizedType tt = (ParameterizedType)t;
					innerType = (Class) tt.getActualTypeArguments()[0];
				}
				
				if(!typeMap.containsKey(innerType)){
					buildNestedType(innerType);
				}
				processField(REPEATED,typeMap.get(fieldType), f.getName(), i);
				continue;
			}
			
			//Ok so not a primitive / scalar, not a map or collection, and we havnt already processed it
			//So it must be another pojo
			buildNestedType(fieldType);
			processField(REPEATED,typeMap.get(fieldType), f.getName(), i);
		}
	}
	
	private void buildNestedType(Class type){
		classStack.push(type);
		buildMessage();
		classStack.pop();
	}
	
	private void buildEntryType(String name, Class innerType, Class innerType2) {
	
		typeMap.put(currentClass(), getPath());
		
		builder.append(getTabs()).append(MESSAGE).append(SPACE).append(name).append(OPEN_BLOCK).append(NEWLINE);
		
		tabDepth++;
		
		if(!typeMap.containsKey(innerType)){
			buildNestedType(innerType);
			typeMap.remove(innerType);
			typeMap.put(innerType, getPath()+PATH_SEPERATOR+name+PATH_SEPERATOR+innerType.getSimpleName());
		}
		processField(REQUIRED,typeMap.get(innerType), "key", 1);
		
		if(!typeMap.containsKey(innerType2)){
			buildNestedType(innerType2);
			typeMap.remove(innerType2);
			typeMap.put(innerType2, getPath()+PATH_SEPERATOR+name+PATH_SEPERATOR+innerType2.getSimpleName());
		}
		processField(REQUIRED,typeMap.get(innerType2), "value", 2);
		
		tabDepth--;
		
		builder.append(getTabs()).append(CLOSE_BLOCK).append(NEWLINE);
	}

	private void processEnum(Class enumType){
		
		classStack.push(enumType);
		typeMap.put(enumType, getPath());
		classStack.pop();
		
		builder.append(getTabs()).append(ENUM).append(SPACE).append(enumType.getSimpleName()).append(OPEN_BLOCK).append(NEWLINE);
		
		tabDepth++;
		
		int i = 0;
		for(Object e : enumType.getEnumConstants()){
			builder.append(getTabs()).append(e.toString()).append(" = ").append(i).append(LINE_END).append(NEWLINE);
		}
		
		tabDepth--;
		
		builder.append(getTabs()).append(CLOSE_BLOCK).append(NEWLINE);
	}
	
	@Override
	/**
	 * If the Proto file has not been generated, generate it. Then return it in string format.
	 * @return String - a String representing the proto file representing this class.
	 */
	public String toString()
	{
		if(builder == null){
			generateProtoFile();
		}
		return builder.toString();
	}

}
