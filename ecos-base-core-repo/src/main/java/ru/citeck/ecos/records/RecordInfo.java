package ru.citeck.ecos.records;

import ru.citeck.ecos.records2.RecordRef;

import java.util.Objects;

public class RecordInfo<T> {

    private RecordRef ref;
    private T data;

    public RecordInfo(RecordRef ref, T data) {
        this.ref = ref;
        this.data = data;
    }

    public RecordRef getRef() {
        return ref;
    }

    public T getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RecordInfo<?> that = (RecordInfo<?>) o;

        return Objects.equals(ref, that.ref);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ref);
    }

    @Override
    public String toString() {
        return "RecordInfo{" +
                "ref=" + ref +
                ", data=" + data +
                '}';
    }
}
