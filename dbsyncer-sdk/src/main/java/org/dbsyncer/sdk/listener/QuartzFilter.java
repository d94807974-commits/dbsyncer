package org.dbsyncer.sdk.listener;

public interface QuartzFilter<T> {

    /**
     * 71. Get DefaultParameter
     */
    T getObject();

    /**
     * CountermeasureParameter
     */
    T getObject(String s);

    /**
     * TurnStringType
     */
    String toString(T value);

    /**
     * YesNoStartField
     */
    boolean begin();
}
