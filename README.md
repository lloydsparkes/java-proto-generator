##Java to Proto

Copyright - Lloyd Sparkes 2012

LICENSE: Public Domain - do as you wish, just retain this message. I just ask that if you find bugs or improve the code, you raise a pull request or an issue, so I can update the code for everyone
				
DISCLAIMER: I am not responsible for your usage of this code, or for any bugs or issues with this code or any resulting side effects

This class simply takes a POJO and creates the associated Proto Buffer Specification File.
  
####Supports:
 		*Nested POJO's
		*Enums
		*Arrays/Collections/Lists/Sets (BUT only if they have a type specifier!! (so List<Byte> is supported, List is not *supported)
		*Maps/KeyValuePairs (BUT they need a type specifier!! (so Map<String,Integer> is supported, Map is not supported)
		*Primitives
		*Boxed Primitives 

####Does Not Support:
		*Nested Collections e.g. Map<List<String>,String>
		*Arrays with more than one Dimension

Usage - CLI:

		java -jar JavaToProto.jar JavaToProto <class name> [<output file name>]
 
 		If output file name is not specified the output will be to the console.
 
 		Ensure that the class name is in the class path somewhere.
 
Usage - Code:
 		
		JavaToProto jpt = new JavaToProto(class name);
		String protoFile = jpt.toString();	


