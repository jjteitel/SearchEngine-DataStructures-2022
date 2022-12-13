package edu.yu.cs.com1320.project.stage5.impl;

import com.google.gson.*;
import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;
import jakarta.xml.bind.DatatypeConverter;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    private class DocumentSerializer implements JsonSerializer<Document> {
        @Override
        public JsonElement serialize(Document document, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonDocument = new JsonObject();
            jsonDocument.addProperty("uri", String.valueOf(document.getKey()));
            if (document.getDocumentTxt() != null) {
                jsonDocument.addProperty("txt", document.getDocumentTxt());
                Gson gson = new Gson();
                JsonElement map = gson.toJsonTree(document.getWordMap());
                jsonDocument.add("wordCountMap", map);
            }else {
                String binaryData = DatatypeConverter.printBase64Binary(document.getDocumentBinaryData());
                jsonDocument.addProperty("binaryData", binaryData);
            }
            return jsonDocument;
        }
    }

    private class DocumentDeserializer implements JsonDeserializer<Document> {
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
    File baseDir;
    Gson gson;

    public DocumentPersistenceManager(File baseDir) {
        if (baseDir == null) {
            this.baseDir = new File(System.getProperty("user.dir"));
        }else {
            this.baseDir = baseDir;
        }
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(DocumentImpl.class, new DocumentSerializer());
        gsonBuilder.registerTypeAdapter(DocumentImpl.class, new DocumentDeserializer());
        this.gson = gsonBuilder.create();
    }

    //Creates a file with the given path and name and serializes the given document to it
    //If the uri is a url, the filepath is the uri without the protocol
    @Override
    public void serialize(URI uri, Document val) throws IOException {
        String path = uri.toString().replace("file://", "");
        path = path.replace("http://", "");
        path = path.replace("https://", "");
        File file = new File(baseDir, path);
        file.mkdirs();
        file.createNewFile();
        FileWriter fileWriter = new FileWriter(file + ".json");
        fileWriter.write(gson.toJson(val));
        fileWriter.close();
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
        String path = uri.toString().replace("file://", "");
        path = path.replace("http://", "");
        path = path.replace("https://", "");
        File file = new File(baseDir.getAbsolutePath() + File.separator + path + ".json");
        if (file.exists()) {
            FileReader fileReader = new FileReader(file);
            Document document = gson.fromJson(fileReader, DocumentImpl.class);
            fileReader.close();
            return document;
        }else {
            return null;
        }
    }

    @Override
    public boolean delete(URI uri) throws IOException {
        String path = uri.toString().replace("file://", "");
        path = path.replace("http://", "");
        path = path.replace("https://", "");
        File file = new File(baseDir.getAbsolutePath() + File.separator + path + ".json");
        if (file.exists()) {
            file.delete();
            return true;
        }else {
            return false;
        }
    }
}
