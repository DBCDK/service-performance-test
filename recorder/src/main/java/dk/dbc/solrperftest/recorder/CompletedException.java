/*
 * Copyright (C) 2019 DBC A/S (http://dbc.dk/)
 *
 * This is part of solr-perf-test-recorder
 *
 * solr-perf-test-recorder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * solr-perf-test-recorder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dbc.solrperftest.recorder;

/**
 *
 * RuntimeException to indicate the duration or the wanted number of output
 * lines has been reached
 *
 *
 * @author Morten Bøgeskov (mb@dbc.dk)
 */
public class CompletedException extends RuntimeException {

    private static final long serialVersionUID = -3553250338040406891L;

    public CompletedException() {
    }
}
