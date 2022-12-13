package edu.yu.cs.com1320.project;

import com.google.gson.*;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.impl.DocumentImpl;
import jakarta.xml.bind.DatatypeConverter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

class serializer implements JsonSerializer<Document> {
    @Override
    public JsonElement serialize(Document document, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonDocument = new JsonObject();
        jsonDocument.addProperty("uri", String.valueOf(document.getKey()));
        if (document.getDocumentTxt() != null) {
            jsonDocument.addProperty("txt", document.getDocumentTxt());
            Gson gson = new Gson();
            JsonElement jsonElement = gson.toJsonTree(document.getWordMap());
            jsonDocument.add("wordCountMap", jsonElement);
        } else {
            String binaryData = DatatypeConverter.printBase64Binary(document.getDocumentBinaryData());
            jsonDocument.addProperty("binaryData", binaryData);
        }
        return jsonDocument;
    }
}

class DocumentDeserializer implements JsonDeserializer<Document> {
    //Deserialize the json element into a document object
    @Override
    public Document deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        URI uri = null;
        try {
            uri = new URI(jsonObject.get("uri").getAsString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        if (jsonObject.get("txt") != null) {
            String txt = jsonObject.get("txt").getAsString();
            JsonObject jsonMap = jsonObject.get("wordCountMap").getAsJsonObject();
            HashMap<String, Integer> wordMap = new Gson().fromJson(jsonMap.toString(), HashMap.class);
            return new DocumentImpl(uri, txt, wordMap);
        }else {
            String jsonBinaryData = jsonObject.get("binaryData").getAsString();
            byte[] binaryData = DatatypeConverter.parseBase64Binary(jsonBinaryData);
            return new DocumentImpl(uri, binaryData);
        }
    }
}

public class SerializerTest {
    public static void main(String[] args) throws URISyntaxException, IOException {
        URI uri = new URI("doc1");
        Document doc = new DocumentImpl(uri, "this is doc1", null);
        Gson gson = new Gson();
        System.out.println(gson.toJson(doc));

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DocumentImpl.class, new serializer());
        gsonBuilder.registerTypeAdapter(DocumentImpl.class, new DocumentDeserializer());
        Gson gson2 = gsonBuilder.create();
        System.out.println(gson2.toJson(doc));

        URI uri2 = new URI("doc2");
        byte[] binaryData = "This is doc2".getBytes();
        Document doc2 = new DocumentImpl(uri2, binaryData);
        System.out.println(gson.toJson(doc2));
        System.out.println(gson2.toJson(doc2));

        System.out.println(System.getProperty("user.dir"));
        FileWriter fileWriter = new FileWriter(System.getProperty("user.dir") + File.separator + uri.toString() + ".json");
        fileWriter.write(gson.toJson(doc));
        fileWriter.close();

        //File file = new File(System.getProperty("user.dir"), uri.toString());
        //FileWriter fileWriter = new FileWriter(file + ".json");
        //System.out.println(file.getName());
        //System.out.println(file.getAbsolutePath());

        String doc2Json = gson2.toJson(doc2);
        Document doc2Deserialized = gson2.fromJson(doc2Json, DocumentImpl.class);
        assert doc2Deserialized == doc2 : "the 2 docs aren't equal";

        String doc1Json = gson2.toJson(doc);
        Document doc1Deserialized = gson2.fromJson(doc1Json, DocumentImpl.class);
        assert doc1Deserialized == doc : "the 2 docs aren't equal";

        URI google = new URI("http://www.google.com");
        System.out.println(google.getScheme());
    }
}
