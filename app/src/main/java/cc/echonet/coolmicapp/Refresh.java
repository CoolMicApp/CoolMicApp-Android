/*
 *      Copyright (C) Jordan Erickson                     - 2014-2016,
 *      Copyright (C) Löwenfelsen UG (haftungsbeschränkt) - 2015-2016
 *       on behalf of Jordan Erickson.
 */

/*
 * This file is part of Cool Mic.
 * 
 * Cool Mic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Cool Mic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Cool Mic.  If not, see <http://www.gnu.org/licenses/>.
 */
package cc.echonet.coolmicapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class Refresh extends Activity {

    DatabaseHandler db;
    CoolMic coolmic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.refresh);
        db = new DatabaseHandler(this);

        coolmic = db.getCoolMicDetails(1);
        String sr = coolmic.getSampleRate();
        coolmic.setSampleRate("8000");
        db.updateCoolMicDetails(coolmic);

        coolmic = db.getCoolMicDetails(1);
        coolmic.setSampleRate("11025");
        db.updateCoolMicDetails(coolmic);

        coolmic = db.getCoolMicDetails(1);
        coolmic.setSampleRate(sr);
        ;
        db.updateCoolMicDetails(coolmic);

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
        Toast.makeText(getApplicationContext(), "Broadcast stopped succesfully !", Toast.LENGTH_LONG).show();
    }
}
