package uk.org.glendale.worldgen.server;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;

@Provider
public class JsonProducer implements ContextResolver<JAXBContext> {
	 private JAXBContext context;
	 private Class[] types = {Foo.class};
	 MessageBodyWriter w;


	public JsonProducer() throws Exception {
		Map<String, Object> props = new HashMap<String, Object>();
	 	//props.put(JSONJAXBContext.JSON_NOTATION, "MAPPED_JETTISON");
	 	//props.put(JSONJAXBContext.JSON_ROOT_UNWRAPPING, Boolean.FALSE);
	 	//this.context = new JSONJAXBContext(types, props);
	 	

	 }



	 public JAXBContext getContext(Class<?> objectType) {
		 return (types[0].equals(objectType)) ? context : null;
	 }
}
