package org.kurento.tutorial.one2onecall.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.kurento.tutorial.one2onecall.UserCallStatus;

public class UserStatusEnumAdapter<T> extends TypeAdapter<T> {

    @Override
    public void write(JsonWriter out, T userStatus) throws IOException {
        if (userStatus == null) {
            out.nullValue();
            return;
        }
        out.value(((UserCallStatus)userStatus).toString());
    }

    @Override
    public T read(JsonReader reader) throws IOException {
        return (T)UserCallStatus.fromStr(reader.nextString());
    }

}
