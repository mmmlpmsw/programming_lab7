package ifmo.programming.lab7.server;

import ifmo.programming.lab7.json.*;
import ifmo.programming.lab7.client.Room;

import java.util.ArrayList;

public class JSONParse {
    static ArrayList<Room> getRoomsFromJSON(String jsonString) throws Exception {

        ArrayList<Room> building = new ArrayList<>();
        JSONEntity entity = JSONParser.parse(jsonString);
        JSONObject object = entity.toObject("Вместо ожидаемого объекта получен элемент типа " + entity.getType().toString().toLowerCase());
        JSONEntity createdEntity = object.getItem("created");

        JSONEntity collectionEntity = object.getItem("collection");
        if (collectionEntity != null) {
            JSONArray collectionArray = collectionEntity.toArray("Вместо ожидаемого массива имеет тип " + collectionEntity.getType().toString().toLowerCase());
//            building.getCollection().clear();

            for (JSONEntity room : collectionArray.getItems()) {

                JSONObject roomObject = room.toObject("Элементы collection должны быть объектами");
                int width = roomObject.getItem("width").toNumber("Поле width элементов коллекции должно быть числом").toInt(),
                        height = roomObject.getItem("height").toNumber("Поле height элементов коллекции должно быть числом, но это").toInt();

                int x = roomObject.getItem("x").toNumber("Координата x элементов коллекции должна быть числом").toInt();
                int y = roomObject.getItem("y").toNumber("Координата y элементов коллекции должна быть числом").toInt();

                String roomName = "";
                JSONEntity roomNameEntity = roomObject.getItem("name");
                if (roomNameEntity != null) {
                    roomName = roomNameEntity.toString("Поле name элементов массива collection должно быть строкой").getContent();
                }

                JSONEntity shelfEntity = roomObject.getItem("shelf");
                Room.Shelf shelf = null;
                String shelfName = "";
                int shelfcount;

                if (shelfEntity != null) {
                    if (!shelfEntity.isObject()) {
                        throw new IllegalArgumentException("shelf должен быть объектом, но имеет тип" + shelfEntity.getType().toString().toLowerCase());
                    }
                    JSONObject shelfObject = (JSONObject) shelfEntity;
                    JSONEntity shelfNameEntity = shelfObject.getItem("name");
                    if (shelfNameEntity != null) {
                        if (shelfNameEntity.isString()) {
                            shelfName = ((JSONString) shelfNameEntity).getContent();
                        } else {
                            throw new IllegalArgumentException("name должен быть строкой, но имеет тип " + shelfNameEntity.getType().toString().toLowerCase());
                        }
                    }

                    JSONEntity shelfThingCountEntity = shelfObject.getItem("thingcount");
                    if (shelfThingCountEntity == null) {
                        shelfcount = 0;
                    }

                    if (shelfThingCountEntity.isNumber()) {
                        shelfcount = (int) ((JSONNumber) shelfThingCountEntity).getValue();
                    } else {
                        throw new IllegalArgumentException("size  должны быть числами, но одно из них имеет тип " + shelfThingCountEntity.getType().toString().toLowerCase());
                    }
                    shelf = new Room.Shelf(shelfcount, shelfName);
                }
                building.add(new Room(width, height, x, y, roomName, shelf));
            }
        }

        return building;

    }

}
