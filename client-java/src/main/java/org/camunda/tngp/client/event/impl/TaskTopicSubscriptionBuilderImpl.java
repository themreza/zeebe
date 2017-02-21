package org.camunda.tngp.client.event.impl;

import org.camunda.tngp.client.event.TaskEventHandler;
import org.camunda.tngp.client.event.TaskTopicSubscriptionBuilder;
import org.camunda.tngp.client.event.TopicEventHandler;
import org.camunda.tngp.client.event.TopicEventType;
import org.camunda.tngp.client.event.TopicSubscription;
import org.camunda.tngp.client.impl.data.MsgPackMapper;
import org.camunda.tngp.util.EnsureUtil;

public class TaskTopicSubscriptionBuilderImpl implements TaskTopicSubscriptionBuilder
{

    protected final TopicSubscriptionBuilderImpl innerBuilder;
    protected final MsgPackMapper msgPackMapper;
    protected TopicEventHandler defaultHandler;
    protected TaskEventHandler taskHandler;

    public TaskTopicSubscriptionBuilderImpl(int topicId, EventAcquisition<TopicSubscriptionImpl> acquisition, MsgPackMapper msgPackMapper)
    {
        innerBuilder = new TopicSubscriptionBuilderImpl(topicId, acquisition);
        this.msgPackMapper = msgPackMapper;
    }

    @Override
    public TaskTopicSubscriptionBuilder defaultHandler(TopicEventHandler handler)
    {
        this.defaultHandler = handler;
        return this;
    }

    @Override
    public TaskTopicSubscriptionBuilder taskEventHandler(TaskEventHandler handler)
    {
        this.taskHandler = handler;
        return this;
    }

    @Override
    public TopicSubscription open()
    {
        EnsureUtil.ensureAtLeastOneNotNull("handlers", defaultHandler, taskHandler);

        innerBuilder.handler(this::dispatchEvent);
        return innerBuilder.open();
    }

    protected void dispatchEvent(TopicEventImpl event) throws Exception
    {
        if (TopicEventType.TASK == event.getEventType() && taskHandler != null)
        {
            final TaskEventImpl taskEventImpl = msgPackMapper.convert(event.getAsMsgPack(), TaskEventImpl.class);
            taskHandler.handle(event, taskEventImpl);
        }
        else if (defaultHandler != null)
        {
            defaultHandler.handle(event, event);
        }
        else
        {
            System.out.println("Ignoring event at position " + event.getEventPosition());
        }
    }
}
