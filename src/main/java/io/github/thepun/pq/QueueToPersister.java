/**
 * Copyright (C)2011 - Marat Gariev <thepun599@gmail.com>
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
package io.github.thepun.pq;

import io.github.thepun.unsafe.MemoryFence;

final class QueueToPersister implements PersistentQueueTail<Object, Object> {

    private final TailCursor tailCursor;

    QueueToPersister(TailCursor tailCursor) {
        this.tailCursor = tailCursor;
    }

    @Override
    public void add(Object element, Object elementContext) {
        TailCursor tailCursorVar = tailCursor;

        long writerIndexVar = tailCursorVar.getCursor();
        long nodeIndexVar = tailCursorVar.getNodeIndex();
        Object[] currentNodeVar = tailCursorVar.getCurrentNode();

        long elementNodeIndex = writerIndexVar >> Node.NODE_DATA_SHIFT;
        if (elementNodeIndex != nodeIndexVar) {
            // get new node
            Object[] newNode = getFreeNode(tailCursorVar);

            tailCursorVar.setNodeIndex(elementNodeIndex);
            tailCursorVar.setCurrentNode(newNode);
            Node.setNext(newNode, null);
            //Node.setPrev(newNode, currentNodeVar);

            // reassure we do not expose new node before it is ready
            MemoryFence.store();

            // attach new node to chain
            Node.setNext(currentNodeVar, newNode);
            currentNodeVar = newNode;
        }

        tailCursorVar.setCursor(writerIndexVar + 2);

        int elementIndex = (int) (writerIndexVar & Node.NODE_DATA_SIZE_MASK);
        Object prev = Node.getElement(currentNodeVar, elementIndex);
        if (prev != null) {
            Object o = null;
        }

        Node.setElement(currentNodeVar, elementIndex | 1, elementContext);
        MemoryFence.store();
        Node.setElement(currentNodeVar, elementIndex, element);
    }

    private static Object[] getFreeNode(TailCursor tailCursor) {
        Object[] externalFreeNodeVar = tailCursor.getFreeNode();

        Object[] nextNode = Node.getNextFree(externalFreeNodeVar);
        if (nextNode == null) {
            return Node.createNew();
        }

        tailCursor.setFreeNode(nextNode);
        return externalFreeNodeVar;
    }
}
