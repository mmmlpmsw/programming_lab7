package ifmo.programming.lab7.client;

import ifmo.programming.lab7.json.*;

/**
 * Советский завод по производству комнат для хрущёвок
 */
class RoomFactory {

    /**
     * создает объект-комнату из json-представления
     * @param json - json-представление объекта
     * @return - объект класса Room
     * @throws JSONParseException
     * @throws IllegalArgumentException
     */
    static Room makeRoomFromJSON(String json) throws JSONParseException, IllegalArgumentException {
        JSONEntity entity = JSONParser.parse(json);
        Room.Shelf shelf;
        if (!entity.isObject()) {
            throw new IllegalArgumentException("Данный json должен быть объектом, но имеет тип " + entity.getType().toString().toLowerCase());
        }

        JSONObject object = (JSONObject)entity;

        JSONEntity coordXEntity = object.getItem("x");
        JSONEntity coordYEntity = object.getItem("y");

        if (coordXEntity == null || coordYEntity == null) {
            throw new IllegalArgumentException("Координаты должны быть обязательно указаны.");
        }

        if (!(coordXEntity.isNumber()&&coordYEntity.isNumber())) {
            throw new IllegalArgumentException("Координаты должны быть целыми числами.");
        }

        int coordX = (int)((JSONNumber)coordXEntity).getValue();
        int coordY = (int)((JSONNumber)coordYEntity).getValue();

        JSONEntity widthEntity = object.getItem("width");
        JSONEntity heightEntity = object.getItem("height");
        //JSONEntity lengthEntity = object.getItem("length");

        if (widthEntity == null || heightEntity == null /*|| lengthEntity == null*/) {
            throw new IllegalArgumentException("width, height должны быть обязательно указаны.");
        }

        if (!(widthEntity.isNumber() && heightEntity.isNumber()/* && lengthEntity.isNumber()*/)) {
            throw new IllegalArgumentException("width, height должны быть числами.");
        }

        int width = (int)((JSONNumber)widthEntity).getValue();
        int height = (int)((JSONNumber)heightEntity).getValue();
        int length = /*(int)((JSONNumber)lengthEntity).getValue();*/ 50;

        String name = "";
        JSONEntity nameEntity = object.getItem("name");
        if (nameEntity != null) {
            if (nameEntity.isString()) {
                name = ((JSONString)nameEntity).getContent();
            }
            else {
                throw new IllegalArgumentException("name должен быть строкой, но имеет тип " + nameEntity.getType().toString().toLowerCase());
            }
        }

        JSONEntity shelfEntity = object.getItem("shelf");
        String shelfName = "";
        int shelfcount;

        if (shelfEntity != null) {
            if (!shelfEntity.isObject()) {
                throw new IllegalArgumentException("shelf должен быть объектом, но имеет тип" + shelfEntity.getType().toString().toLowerCase());
            }
            JSONObject shelfObject = (JSONObject)shelfEntity;
            JSONEntity shelfNameEntity = shelfObject.getItem("name");
            if (shelfNameEntity != null) {
                if (shelfNameEntity.isString()) {
                    shelfName = ((JSONString)shelfNameEntity).getContent();
                }
                else {
                    throw new IllegalArgumentException("name должен быть строкой, но имеет тип " + nameEntity.getType().toString().toLowerCase());
                }
            }

            JSONEntity shelfThingCountEntity = shelfObject.getItem("thingcount");
            if (shelfThingCountEntity == null ) {
                shelfcount = 0;
            }

            if (shelfThingCountEntity.isNumber()) { shelfcount = (int)((JSONNumber) shelfThingCountEntity).getValue();}
            else {
                throw new IllegalArgumentException("size  должны быть числами, но одно из них имеет тип " + shelfThingCountEntity.getType().toString().toLowerCase());
            }
            shelf = new Room.Shelf(shelfcount, shelfName);
        } else shelf = null;

        return new Room(width, height, coordX, coordY,  name, shelf);
    }

    /**
     * Создаёт комнаты из их json-представления. Если получен json-объект, сгенерируется одна комната.
     * Если получен json-массив объектов, будет прочтён каждый объект внутри массива и возвращён
     * массив комнат, сгенерированных для каждого объекта
     * @param json json-представление
     * @return массив комнат
     * @throws Exception если что-то пошло не по плану
     */
    static Room[] makeRoomsFromJSON (String json) throws Exception {
        JSONEntity entity = JSONParser.parse(json);

        if (entity == null) {throw new IllegalArgumentException("Требуется json-объект, но получен null"); }
        if (entity.isObject()) { return new Room[]{makeRoomFromJSON(entity.toString())}; }
        else {
            if (entity.isArray()) {
                JSONArray roomArray = entity.toArray();
                Room[] rooms = new Room[roomArray.size()];
                for (int i = 0; i < roomArray.size(); i++) {
                    rooms[i] = makeRoomFromJSON(String.valueOf(roomArray.getItem(i).toObject()));
                }
                return rooms;
            }
            else {
                throw new IllegalArgumentException("Ошибка: не все элементы массива являются объектами.");
            }
        }
    }
}
