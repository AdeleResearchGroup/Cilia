/*
 * Copyright Adele Team LIG
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.liglab.adele.cilia.runtime.impl;

import fr.liglab.adele.cilia.AdminData;
import fr.liglab.adele.cilia.util.concurrent.ReadWriteLock;
import fr.liglab.adele.cilia.util.concurrent.WriterPreferenceReadWriteLock;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:cilia-devel@lists.ligforge.imag.fr">Cilia Project
 *         Team</a>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class AdminDataImpl implements AdminData {

    private final Map m_dataChain = new HashMap();
    private final Map m_dataChainLocked = new HashMap();

    private ReadWriteLock _lock = new WriterPreferenceReadWriteLock();

    private void m_createMediatorMap(String mediatorId) {
        try {
            _lock.writeLock().acquire();
            try {
                m_dataChain.put(mediatorId, new HashMap());
                m_dataChainLocked.put(mediatorId, new HashMap());
            } finally {
                _lock.writeLock().release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e.getMessage());
        }
    }

    private Map m_getData(String mediatorId, boolean isRegular) {
        Map dataMediator = null;
        Map dataContainer;
        try {
            _lock.readLock().acquire();
            try {
                if (isRegular)
                    dataContainer = m_dataChain;
                else
                    dataContainer = m_dataChainLocked;

                return (Map) dataContainer.get(mediatorId);

            } finally {
                _lock.readLock().release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e.getMessage());
        }

    }

    public Map getData(String mediatorId, boolean isRegular) {
        Map dataMediator;
        if ((mediatorId == null) || (mediatorId.length() == 0))
            throw new IllegalArgumentException("parameter must not be null");

        dataMediator = m_getData(mediatorId, isRegular);
        if (dataMediator == null) {
            m_createMediatorMap(mediatorId);
            dataMediator = m_getData(mediatorId, isRegular);
        }
        return dataMediator;
    }

    private void m_clearData(String mediatorId, boolean isRegular) {
        Map data, dataContainer;
        if (isRegular)
            dataContainer = m_dataChain;
        else
            dataContainer = m_dataChainLocked;

        data = (Map) dataContainer.remove(mediatorId);
        if (data != null)
            data.clear();
    }

    public void copyData(String mediatorSource, String mediatorDest) {
        if ((mediatorSource != null) && (mediatorDest != null)) {
            try {
                _lock.writeLock().acquire();
                try {
                    if (m_dataChain.containsKey(mediatorSource)) {
                        Map data = (Map) m_dataChain.get(mediatorSource);
                        m_dataChain.put(mediatorDest, new HashMap(data));
                        data = (Map) m_dataChainLocked.get(mediatorSource);
                        m_dataChainLocked.put(mediatorDest, new HashMap(data));
                    } else {
                        m_dataChain.put(mediatorDest, new HashMap());
                        m_dataChainLocked.put(mediatorDest, new HashMap());
                    }

                } finally {
                    _lock.writeLock().release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    public void clearData(String mediatorId) {
        if ((mediatorId != null) && (mediatorId.length() != 0)) {
            try {
                _lock.writeLock().acquire();
                try {
                    m_clearData(mediatorId, true);
                    m_clearData(mediatorId, false);
                } finally {
                    _lock.writeLock().release();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    public void start() {
    }

    public void stop() {
        /* Clear all data */
        try {
            _lock.writeLock().acquire();
            try {
                m_dataChain.clear();
                m_dataChainLocked.clear();
            } finally {
                _lock.writeLock().release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e.getMessage());
        }
    }
}
