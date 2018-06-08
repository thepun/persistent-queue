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

final class Logger {

    static void info(String message, Object ... params) {
        System.out.println("INFO: " + fillPlaceholders(message, params));
    }

    static void warn(String message, Object ... params) {
        System.err.println("WARN: " + fillPlaceholders(message, params));
    }

    static void error(String message, Object ... params) {
        System.err.println("ERROR: " + fillPlaceholders(message, params));
    }

    static void error(Throwable e, String message, Object ... params) {
        System.err.println("ERROR: " + fillPlaceholders(message, params));
        e.printStackTrace(System.err);
    }

    static String fillPlaceholders(String message, Object ... params) {
        for (Object param : params) {
            message = message.replaceFirst("\\{\\}", param.toString());
        }
        return message;
    }
}
