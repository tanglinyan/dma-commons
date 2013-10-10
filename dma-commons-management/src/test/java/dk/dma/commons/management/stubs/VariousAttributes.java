/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.commons.management.stubs;

import java.io.IOException;

import dk.dma.commons.management.Description;
import dk.dma.commons.management.ManagedAttribute;

/**
 * @author Kasper Nielsen
 */
public class VariousAttributes {
    private String none;

    private boolean readOnly;

    private Integer readWrite;

    private String writeOnly;

    public String getError() {
        throw new LinkageError();
    }

    @ManagedAttribute()
    public String getException2() throws Exception {
        throw new IOException();
    }

    public String getNone() {
        return none;
    }

    public Integer getReadWrite() {
        return readWrite;
    }

    public String getRuntimeException() {
        throw new IllegalMonitorStateException();
    }

    public String getWriteOnly() {
        return writeOnly;
    }

    @ManagedAttribute
    public boolean isReadOnly() {
        return readOnly;
    }

    @Description("desc")
    @ManagedAttribute(name = "throwError")
    public void setError(String ignore) {
        throw new LinkageError();
    }

    @ManagedAttribute()
    public void setException1(String re) throws Exception {
        throw new IOException();
    }

    public void setNone(String none) {
        this.none = none;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @ManagedAttribute()
    public void setReadWrite(Integer readWrite) {
        this.readWrite = readWrite;
    }

    @Description("desc")
    @ManagedAttribute(name = "throwRuntimeException")
    public void setRuntimeException(String re) {
        throw new IllegalMonitorStateException();
    }

    @ManagedAttribute(isWriteOnly = true)
    public void setWriteOnly(String writeOnly) {
        this.writeOnly = writeOnly;
    }
}
