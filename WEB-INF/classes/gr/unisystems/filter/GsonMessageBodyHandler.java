package gr.unisystems.filter;

import javax.ws.rs.WebApplicationException;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.InputStreamReader;
import java.io.InputStream;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.Gson;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

public class GsonMessageBodyHandler implements MessageBodyWriter<Object>, MessageBodyReader<Object>{
    private Gson gson;
    
    private Gson getGson() {
        if (this.gson == null) {
            final GsonBuilder gsonBuilder = new GsonBuilder();
            this.gson = gsonBuilder.disableHtmlEscaping().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).setPrettyPrinting().serializeNulls().create();
        }
        return this.gson;
    }
    
    public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return true;
    }
    
    public Object readFrom(final Class<Object> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders, final InputStream entityStream) {
        InputStreamReader streamReader = null;
        try {
            streamReader = new InputStreamReader(entityStream, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            Type jsonType;
            if (type.equals(genericType)) {
                jsonType = type;
            }
            else {
                jsonType = genericType;
            }
            return this.getGson().fromJson((Reader)streamReader, jsonType);
        }
        finally {
            try {
                streamReader.close();
            }
            catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }
    
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return true;
    }
    
    public long getSize(final Object object, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return -1L;
    }
    
    public void writeTo(final Object object, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream) throws IOException, WebApplicationException {
        final OutputStreamWriter writer = new OutputStreamWriter(entityStream, "UTF-8");
        try {
            Type jsonType;
            if (type.equals(genericType)) {
                jsonType = type;
            }
            else {
                jsonType = genericType;
            }
            this.getGson().toJson(object, jsonType, (Appendable)writer);
        }
        finally {
            writer.close();
        }
        writer.close();
    }
}
