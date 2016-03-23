package org.camunda.tngp.transport.impl;

import org.camunda.tngp.dispatcher.Dispatcher;
import org.camunda.tngp.transport.ChannelReceiveHandler;
import org.camunda.tngp.transport.impl.agent.ReceiverCmd;
import org.camunda.tngp.transport.impl.agent.SenderCmd;
import org.camunda.tngp.transport.impl.agent.TransportConductorCmd;

import uk.co.real_logic.agrona.concurrent.AgentRunner;
import uk.co.real_logic.agrona.concurrent.ManyToOneConcurrentArrayQueue;

public class TransportContext
{
    protected ManyToOneConcurrentArrayQueue<ReceiverCmd> receiverCmdQueue = new ManyToOneConcurrentArrayQueue<>(100);
    protected ManyToOneConcurrentArrayQueue<SenderCmd> senderCmdQueue = new ManyToOneConcurrentArrayQueue<>(100);
    protected ManyToOneConcurrentArrayQueue<TransportConductorCmd> conductorCmdQueue = new ManyToOneConcurrentArrayQueue<>(100);

    protected Dispatcher sendBuffer;
    protected Dispatcher receiveBuffer;
    protected ChannelReceiveHandler channelReceiveHandler;

    protected int maxMessageLength;

    protected AgentRunner[] agentRunners;

    public void setSendBuffer(Dispatcher sendBuffer)
    {
        this.sendBuffer = sendBuffer;
    }

    public Dispatcher getSendBuffer()
    {
        return sendBuffer;
    }

    public ManyToOneConcurrentArrayQueue<ReceiverCmd> getReceiverCmdQueue()
    {
        return receiverCmdQueue;
    }

    public void setReceiverCmdQueue(ManyToOneConcurrentArrayQueue<ReceiverCmd> receiverCmdQueue)
    {
        this.receiverCmdQueue = receiverCmdQueue;
    }

    public ManyToOneConcurrentArrayQueue<SenderCmd> getSenderCmdQueue()
    {
        return senderCmdQueue;
    }

    public void setSenderCmdQueue(ManyToOneConcurrentArrayQueue<SenderCmd> senderCmdQueue)
    {
        this.senderCmdQueue = senderCmdQueue;
    }

    public int getMaxMessageLength()
    {
        return maxMessageLength;
    }

    public void setMaxMessageLength(int maxMessageLength)
    {
        this.maxMessageLength = maxMessageLength;
    }

    public AgentRunner[] getAgentRunners()
    {
        return agentRunners;
    }

    public void setAgentRunners(AgentRunner[] agentRunners)
    {
        this.agentRunners = agentRunners;
    }

    public ManyToOneConcurrentArrayQueue<TransportConductorCmd> getConductorCmdQueue()
    {
        return conductorCmdQueue;
    }

    public void setConductorCmdQueue(ManyToOneConcurrentArrayQueue<TransportConductorCmd> clientConductorCmdQueue)
    {
        this.conductorCmdQueue = clientConductorCmdQueue;
    }

    public void setReceiveBuffer(Dispatcher receiveBuffer)
    {
        this.receiveBuffer = receiveBuffer;
    }

    public Dispatcher getReceiveBuffer()
    {
        return receiveBuffer;
    }

    public ChannelReceiveHandler getChannelReceiveHandler()
    {
        return channelReceiveHandler;
    }

    public void setChannelReceiveHandler(ChannelReceiveHandler channelReceiveHandler)
    {
        this.channelReceiveHandler = channelReceiveHandler;
    }
}
