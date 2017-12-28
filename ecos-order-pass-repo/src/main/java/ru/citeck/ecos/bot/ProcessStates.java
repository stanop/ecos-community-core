package ru.citeck.ecos.bot;

public interface ProcessStates {
    int UNAUTHORIZED = -1;
    int AUTORIZATION_REQUEST = 0;
    int STARTSTATE = 1;
    int FIO_STATE = 2;
    int DATE_STATE = 3;
    int DATA_RECEIVED = 4;
}
