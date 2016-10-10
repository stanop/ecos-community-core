package ru.citeck.ecos.utils.converter.amount;

/**
 * @author Roman.Makarskiy on 10.07.2016.
 */
 abstract class Currency {

    private String fractional1, fractional2, fractional3;
    private String intact1, intact2, intact3;

    public Currency () {
        initializationResources();
    }

    abstract void initializationResources  ();

    String getFractional1() {
        return fractional1;
    }

    void setFractional1(String fractional1) {
        this.fractional1 = fractional1;
    }

    String getFractional2() {
        return fractional2;
    }

    void setFractional2(String fractional2) {
        this.fractional2 = fractional2;
    }

    String getFractional3() {
        return fractional3;
    }

    void setFractional3(String fractional3) {
        this.fractional3 = fractional3;
    }

    String getIntact1() {
        return intact1;
    }

    void setIntact1(String intact1) {
        this.intact1 = intact1;
    }

    String getIntact2() {
        return intact2;
    }

    void setIntact2(String intact2) {
        this.intact2 = intact2;
    }

    String getIntact3() {
        return intact3;
    }

    void setIntact3(String intact3) {
        this.intact3 = intact3;
    }
}
