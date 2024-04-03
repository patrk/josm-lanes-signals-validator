plugins {
    id("org.openstreetmap.josm") version "0.8.2"
}

josm {
    josmCompileVersion = "tested"
    manifest {
        description = "Lanes, turn lanes and traffic signals validations"
        mainClass = "org.openstreetmap.josm.plugins.lanes_and_traffic_signals_validation.LanesTrafficSignalsValidationPlugin"
        minJosmVersion = "17569"
        author = "Patryk Mikulski"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}
