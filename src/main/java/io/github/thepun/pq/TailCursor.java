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

final class TailCursor extends TailCursorMid3 {

    private long p0, p1, p2, p3, p4, p5, p6, p7;

}

class TailCursorPre {

    private long p0, p1, p2, p3, p4, p5;

}

class TailCursorMid1 extends TailCursorPre {

    private long cursor;
    private long nodeIndex;
    private Object[] currentNode;

    long getCursor() {
        return cursor;
    }

    void setCursor(long cursor) {
        this.cursor = cursor;
    }

    long getNodeIndex() {
        return nodeIndex;
    }

    void setNodeIndex(long nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    Object[] getCurrentNode() {
        return currentNode;
    }

    void setCurrentNode(Object[] currentNode) {
        this.currentNode = currentNode;
    }
}

class TailCursorMid2 extends TailCursorMid1 {

    private long p0, p1, p2, p3, p4, p5, p6, p7;

}

class TailCursorMid3 extends TailCursorMid2 {

    private Object[] freeNode;

    Object[] getFreeNode() {
        return freeNode;
    }

    void setFreeNode(Object[] freeNode) {
        this.freeNode = freeNode;
    }

}