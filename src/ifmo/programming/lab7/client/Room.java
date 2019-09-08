package ifmo.programming.lab7.client;

import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

public class Room implements Comparable<Room>, Serializable {

    private String name /*= "Безымянная";*/;
    private int height, width, depth = 50; //depth - фиксированнвя глубина, width - ширина, height - высота
    private int x, y; //только по длине и высоте
//    private Clock clock = Clock.system(ZoneId.of("Europe/Moscow"));
    private LocalDateTime creationDate = LocalDateTime.now();
    private Shelf shelf;
    private int size;

    Room(int width, int height, int x, int y) {
        setBounds(x, y, width, height);
    }

    Room(int width, int height, int x, int y, String name) {
        setBounds(x, y, width, height);
        setName(name);
    }

    public Room(int width, int height, int x, int y, String name, Shelf shelf) {
        setBounds(x, y, width, height);
        size = width*height*depth;
        setName(name);
        this.shelf = shelf;
    }

    public Shelf getShelf() { return shelf; }


    public void setBounds(int x, int y, int width, int height) {
        setPosition(x, y);
        setSize(width, height);
    }

    public void setPosition(int x, int y) {
        setX(x);
        setY(y);
    }

    public void setSize(int width, int height) {
        setWidth(width);
        setHeight(height);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public int getDepth() { return depth; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void setWidth(int width) {
        if (width < 0)
            throw new IllegalArgumentException("Ширина не может быть отрицательной");
        this.width = width;
    }

    public void setHeight(int height) {
        if (height < 0)
            throw new IllegalArgumentException("Высота не может быть отрицательной");
        this.height = height;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getCreationDate() {return creationDate; }

    /**
     *осуществляет сортировку комнат по имени
     */
    @Override
    public int compareTo(Room o) {
        return this.getName().compareTo(o.getName());
    }

    /**
     * читабельное представление об объекте
     * @return String - информация о комнате
     */
    @Override
    public String toString() {

        StringBuilder roominfo = new StringBuilder("Комната");
        if (name.isEmpty()) {
            roominfo.append("без названия");
        } else {
            roominfo.append("-" + this.getName());
        }
        roominfo.append(", имеющая размеры: " + width + " x " + height + " x " + depth + ", ")
                .append("и координаты левой нижней точки: x: " + getX() + ", y: " + getY() );
        if (shelf.getThingcount() == 0) {roominfo.append(" , пустая.");}
        else {
            roominfo.append(", содержащая " + shelf.getThingcount() + " предметов.");
        }
        return roominfo.toString();

    }


    public static /*static*/ class Shelf implements Serializable {
        private int thingcount;
        private String name;

        public Shelf(String name) {
            setName(name);
        }

        public Shelf() {}

        public Shelf(int thingcount, String name) {
            setName(name);
            setThingcount(thingcount);
        }

        public void setThingcount(int thingcount) {this.thingcount = thingcount;}
        public int getThingcount() {return thingcount;}

        public String getName() {return name;}
        public void setName(String name) {this.name = name;}

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Shelf shelf = (Shelf) o;
            return thingcount == shelf.thingcount &&
                    Objects.equals(name, shelf.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(thingcount, name);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return height == room.height &&
                width == room.width &&
                depth == room.depth &&
                x == room.x &&
                y == room.y &&
                Objects.equals(name, room.name) &&
                Objects.equals(shelf, room.shelf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, height, width, depth, x, y, creationDate, shelf);
    }
}
