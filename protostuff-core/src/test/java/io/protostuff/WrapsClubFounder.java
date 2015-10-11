/**
 * Copyright (C) 2007-2015 Protostuff
 * http://www.protostuff.io/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.protostuff;

import java.io.Serializable;

/**
 * Used for testing regular pojos that wraps messages (with schema).
 * 
 * @author David Yu
 */
public class WrapsClubFounder implements Serializable
{

    private static final long serialVersionUID = 1L;

    private ClubFounder clubFounder;

    public WrapsClubFounder()
    {

    }

    public WrapsClubFounder(ClubFounder clubFounder)
    {
        this.clubFounder = clubFounder;
    }

    public ClubFounder getClubFounder()
    {
        return clubFounder;
    }

    public void setClubFounder(ClubFounder clubFounder)
    {
        this.clubFounder = clubFounder;
    }

}
