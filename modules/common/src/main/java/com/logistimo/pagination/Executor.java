/*
 * Copyright © 2017 Logistimo.
 *
 * This file is part of Logistimo.
 *
 * Logistimo software is a mobile & web platform for supply chain management and remote temperature monitoring in
 * low-resource settings, made available under the terms of the GNU Affero General Public License (AGPL).
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * You can be released from the requirements of the license by purchasing a commercial license. To know more about
 * the commercial license, please contact us at opensource@logistimo.com
 */

package com.logistimo.pagination;

import com.logistimo.services.utils.ConfigUtil;

import com.logistimo.pagination.processor.ProcessingException;
import com.logistimo.pagination.processor.Processor;
import com.logistimo.exception.TaskSchedulingException;

/**
 * @author Mohan Raja
 */
public class Executor {
  private static boolean isGAE = ConfigUtil.isGAE();

  public static void exec(Long domainId, QueryParams qp, PageParams pageParams,
                          String processorClassName, String prevOutput,
                          PagedExec.Finalizer finalizer)
      throws TaskSchedulingException, ProcessingException {
    if (isGAE) {
      PagedExec.exec(domainId, qp, pageParams, processorClassName, prevOutput, finalizer);
    } else {
      StreamingExecutor.exec(domainId, qp, null, processorClassName, prevOutput, finalizer);
    }
  }

  public static void exec(Long domainId, QueryParams qp, PageParams pageParams,
                          String processorClassName, String prevOutput,
                          PagedExec.Finalizer finalizer, int secondsBetweenTasks)
      throws TaskSchedulingException, ProcessingException {
    if (isGAE) {
      PagedExec.exec(domainId, qp, pageParams, processorClassName, prevOutput, finalizer,
          secondsBetweenTasks);
    } else {
      StreamingExecutor
          .exec(domainId, qp, null, processorClassName, prevOutput, finalizer, secondsBetweenTasks);
    }
  }

  public static void exec(Long domainId, QueryParams qp, PageParams pageParams,
                          Processor proc, String prevOutput, PagedExec.Finalizer finalizer,
                          int secondsBetweenTasks, boolean async)
      throws TaskSchedulingException, ProcessingException {
    if (isGAE) {
      PagedExec
          .exec(domainId, qp, pageParams, proc, prevOutput, finalizer, secondsBetweenTasks, async);
    } else {
      StreamingExecutor
          .exec(domainId, qp, null, proc, prevOutput, finalizer, secondsBetweenTasks, async);
    }
  }
}
