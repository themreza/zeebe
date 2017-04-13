package org.camunda.tngp.broker.taskqueue.data;

import org.agrona.DirectBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.camunda.tngp.broker.util.msgpack.UnpackedObject;
import org.camunda.tngp.broker.util.msgpack.property.BinaryProperty;
import org.camunda.tngp.broker.util.msgpack.property.EnumProperty;
import org.camunda.tngp.broker.util.msgpack.property.IntegerProperty;
import org.camunda.tngp.broker.util.msgpack.property.LongProperty;
import org.camunda.tngp.broker.util.msgpack.property.PackedProperty;
import org.camunda.tngp.broker.util.msgpack.property.StringProperty;
import org.camunda.tngp.msgpack.spec.MsgPackHelper;
import org.camunda.tngp.protocol.Protocol;

public class TaskEvent extends UnpackedObject
{
    public static final MutableDirectBuffer EMPTY_MAP = new UnsafeBuffer(MsgPackHelper.EMTPY_OBJECT);
    protected static final DirectBuffer EMPTY_PAYLOAD = new UnsafeBuffer(MsgPackHelper.EMTPY_OBJECT);

    private final EnumProperty<TaskEventType> eventTypeProp = new EnumProperty<>("eventType", TaskEventType.class);
    private final LongProperty lockTimeProp = new LongProperty("lockTime", Protocol.INSTANT_NULL_VALUE);
    private final IntegerProperty lockOwnerProp = new IntegerProperty("lockOwner", -1);
    private final IntegerProperty retriesProp = new IntegerProperty("retries", -1);
    private final StringProperty typeProp = new StringProperty("type");
    private final PackedProperty headersProp = new PackedProperty("headers", EMPTY_MAP);
    private final BinaryProperty payloadProp = new BinaryProperty("payload", EMPTY_PAYLOAD);

    private final TaskHeaders taskHeaders = new TaskHeaders();
    private final UnsafeBuffer taskHeadersBuffer = new UnsafeBuffer(0, 0);

    public TaskEvent()
    {
        this.declareProperty(eventTypeProp)
            .declareProperty(lockTimeProp)
            .declareProperty(lockOwnerProp)
            .declareProperty(retriesProp)
            .declareProperty(typeProp)
            .declareProperty(headersProp)
            .declareProperty(payloadProp);
    }

    public TaskEventType getEventType()
    {
        return eventTypeProp.getValue();
    }

    public TaskEvent setEventType(TaskEventType type)
    {
        eventTypeProp.setValue(type);
        return this;
    }

    public long getLockTime()
    {
        return lockTimeProp.getValue();
    }

    public TaskEvent setLockTime(long val)
    {
        lockTimeProp.setValue(val);
        return this;
    }

    public int getLockOwner()
    {
        return lockOwnerProp.getValue();
    }

    public TaskEvent setLockOwner(int lockOwer)
    {
        lockOwnerProp.setValue(lockOwer);
        return this;
    }

    public int getRetries()
    {
        return retriesProp.getValue();
    }

    public TaskEvent setRetries(int retries)
    {
        retriesProp.setValue(retries);
        return this;
    }

    public DirectBuffer getType()
    {
        return typeProp.getValue();
    }

    public TaskEvent setType(DirectBuffer buf)
    {
        return setType(buf, 0, buf.capacity());
    }

    public TaskEvent setType(DirectBuffer buf, int offset, int length)
    {
        typeProp.setValue(buf, offset, length);
        return this;
    }

    public DirectBuffer getPayload()
    {
        return payloadProp.getValue();
    }

    public TaskEvent setPayload(DirectBuffer payload)
    {
        payloadProp.setValue(payload);
        return this;
    }

    public TaskEvent setPayload(DirectBuffer payload, int offset, int length)
    {
        payloadProp.setValue(payload, offset, length);
        return this;
    }

    public TaskHeaders getHeaders()
    {
        taskHeaders.wrap(headersProp.getValue());
        return taskHeaders;
    }

    public TaskEvent setHeaders(TaskHeaders taskHeaders)
    {
        // TODO #203 - replace by general unpack mechanism
        final int length = taskHeaders.getLength();

        taskHeadersBuffer.wrap(new byte[length]);
        taskHeaders.write(taskHeadersBuffer, 0);

        headersProp.setValue(taskHeadersBuffer, 0, length);
        return this;
    }

}

