import java.sql.Timestamp

void registerAdditionalArithmeticOps() {
    Timestamp.metaClass.plus { Long l ->
        new Timestamp(new Date((delegate as Timestamp).time + l).time)
    }

    Timestamp.metaClass.minus { Long l ->
        new Timestamp(new Date((delegate as Timestamp).time - l).time)
    }

    Timestamp.metaClass.minus { Timestamp t ->
        (((delegate as Timestamp).time - t.time)) as Long
    }

    Timestamp.metaClass.plus { Timestamp t ->
        (((delegate as Timestamp).time + t.time)) as Long
    }

    Long.metaClass.plus { Timestamp t ->
        new Timestamp(t.time + (delegate as Long))
    }

    Long.metaClass.minus { Timestamp t ->
        new Timestamp(t.time - (delegate as Long))
    }
}

registerAdditionalArithmeticOps()