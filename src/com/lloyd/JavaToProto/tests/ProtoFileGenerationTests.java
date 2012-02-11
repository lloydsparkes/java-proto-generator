package com.lloyd.JavaToProto.tests;

import org.junit.Test;

import com.lloyd.JavaToProto.*;
import com.lloyd.JavaToProto.tests.samples.*;

public class ProtoFileGenerationTests {

	@Test
	public void SimpleTest(){
		JavaToProto jtp = new JavaToProto(SimpleMessage.class);
		System.out.println(jtp.toString());
	}
	
	
	@Test
	public void ArrayTest(){
		JavaToProto jtp = new JavaToProto(ArrayMessage.class);
		System.out.println(jtp.toString());
	}
	
	
	@Test
	public void ListTest(){
		JavaToProto jtp = new JavaToProto(ListMessage.class);
		System.out.println(jtp.toString());
	}
	
	
	@Test
	public void MapTest(){
		JavaToProto jtp = new JavaToProto(MapMessage.class);
		System.out.println(jtp.toString());
	}
	
	
	@Test
	public void POJOTest(){
		JavaToProto jtp = new JavaToProto(POJOMessage.class);
		System.out.println(jtp.toString());
	}
	
}
