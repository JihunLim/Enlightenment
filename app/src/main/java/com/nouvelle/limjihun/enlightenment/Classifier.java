
package com.nouvelle.limjihun.enlightenment;

import android.graphics.Bitmap;
import android.graphics.RectF;

import java.util.List;

/**
 * Created by LimJiHun on 2017-08-18.
 * <!--
 ~    Copyright (C) 2017 Hooni Nouvelle Indi Developer
 ~
 ~    Licensed under the Apache License, Version 2.0 (the "License");
 ~    you may not use this file except in compliance with the License.
 ~    You may obtain a copy of the License at
 ~
 ~        http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~    Unless required by applicable law or agreed to in writing, software
 ~    distributed under the License is distributed on an "AS IS" BASIS,
 ~    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~    See the License for the specific language governing permissions and
 ~    limitations under the License.
 -->
 */

public interface Classifier {

    List<Recognition> recognizeImage(Bitmap bitmap);
    String getStatString();
    void enableStatLogging(final boolean debug);
    void close();

    public class Recognition {

        private final String id;
        private final String title;
        private final Float confidence;

        private RectF location;

        public Recognition(final String id, final String title, final Float confidence, final RectF location) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
            this.location = location;
        }

        public String getTitle() {
            return title;
        }

        public Float getConfidence() {
            return confidence;
        }

        public String getId() {
            return id;
        }

        public RectF getLocation() {
            return location;
        }

        public void setLocation(RectF location) {
            this.location = location;
        }

        public String toEnString(int i) {
            String resultString = "";
            if (title != null)
                resultString += (i+1)+ ". " + title + " ";
            if (id != null)
                resultString += "[" + id + "번] ";
            if (confidence != null)
                resultString += String.format("(%.1f%%)", confidence * 100.0f);
            if (location != null)
                resultString += location + "\n";
            return resultString.trim();
        }

        public String toKRString(){
            String resultString = "";
            if (title != null)
                resultString += title;
            return resultString.trim();
        }

    }
}
