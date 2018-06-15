/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.gossip.membership;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import io.zeebe.gossip.GossipMembershipListener;
import io.zeebe.transport.SocketAddress;

public class MembershipList implements Iterable<Member>
{
    private final Member self;

    private final List<Member> members = new ArrayList<>();
    private final List<Member> membersView = Collections.unmodifiableList(members);

    private final AliveMembershipIterator iterator = new AliveMembershipIterator();

    private final Consumer<Member> onSuspectedMember;
    private final List<GossipMembershipListener> listeners = new ArrayList<>();

    private int aliveMemberSize = 0;

    public MembershipList(SocketAddress address, Consumer<Member> onSuspectedMember)
    {
        this.self = new Member(address);
        this.onSuspectedMember = onSuspectedMember;
    }

    public Member self()
    {
        return self;
    }

    public boolean hasMember(SocketAddress address)
    {
        return get(address) != null;
    }

    public Member get(SocketAddress address)
    {
        for (Member member : members)
        {
            if (member.getAddress().equals(address))
            {
                return member;
            }
        }
        return null;
    }

    public Member getMemberOrSelf(SocketAddress address)
    {
        if (address.equals(self.getAddress()))
        {
            return self;
        }
        else
        {
            return get(address);
        }
    }

    public Member newMember(SocketAddress address, GossipTerm term)
    {
        final Member member = new Member(address);
        member.getTerm().wrap(term);

        members.add(member);
        aliveMemberSize += 1;

        for (GossipMembershipListener listener : listeners)
        {
            listener.onAdd(member);
        }

        return member;
    }

    public void removeMember(SocketAddress address)
    {
        final Member member = get(address);
        if (member != null)
        {
            // keep the member in the list (for now) to avoid that an old ALIVE event add it again
            member.setStatus(MembershipStatus.DEAD);
            aliveMemberSize -= 1;

            for (GossipMembershipListener listener : listeners)
            {
                listener.onRemove(member);
            }
        }
    }

    public void aliveMember(SocketAddress address, GossipTerm gossipTerm)
    {
        final Member member = get(address);
        if (member != null)
        {
            final boolean isUndead = member.getStatus() == MembershipStatus.DEAD;

            member
                .setStatus(MembershipStatus.ALIVE)
                .setGossipTerm(gossipTerm);

            if (isUndead)
            {
                aliveMemberSize += 1;

                for (GossipMembershipListener listener : listeners)
                {
                    listener.onAdd(member);
                }
            }
        }
    }

    public void suspectMember(SocketAddress address, GossipTerm gossipTerm)
    {
        final Member member = get(address);
        if (member != null)
        {
            member
                .setStatus(MembershipStatus.SUSPECT)
                .setGossipTerm(gossipTerm);

            onSuspectedMember.accept(member);
        }
    }

    public int size()
    {
        return aliveMemberSize;
    }

    public List<Member> getMembersView()
    {
        return membersView;
    }

    public void addListener(GossipMembershipListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(GossipMembershipListener listener)
    {
        listeners.remove(listener);
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("MembershipList [self=");
        builder.append(self);
        builder.append(", members=");
        builder.append(members);
        builder.append("]");
        return builder.toString();
    }


    @Override
    public Iterator<Member> iterator()
    {
        iterator.reset();
        return iterator;
    }

    private class AliveMembershipIterator implements Iterator<Member>
    {
        private int index = 0;
        private int aliveCount = 0;

        public void reset()
        {
            index = 0;
            aliveCount = 0;
        }

        @Override
        public boolean hasNext()
        {
            return aliveCount < aliveMemberSize;
        }

        @Override
        public Member next()
        {
            if (hasNext())
            {
                // only return alive members
                Member member = null;
                do
                {
                    member = members.get(index);
                    index += 1;
                }
                while (member.getStatus() == MembershipStatus.DEAD);

                aliveCount += 1;

                return member;
            }
            else
            {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove()
        {
            index -= 1;

            final Member member = members.get(index);
            removeMember(member.getAddress());
        }

    }

}
