package au.org.ala.soils2sat

class LayerSet {

    String name
    User user
    String description

    static hasMany = [layers:String]

    static constraints = {
        name nullable: false
        user nullable: true
        layers nullable: true
        description nullable: true
    }
}
