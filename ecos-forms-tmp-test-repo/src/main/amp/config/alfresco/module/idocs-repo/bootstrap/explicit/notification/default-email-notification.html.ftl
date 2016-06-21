<#escape x as x?html>
<html>
   <head>
      <style type="text/css"><!--
      body
      {
         font-family: Arial, sans-serif;
         font-size: 14px;
         color: #4c4c4c;
      }
      
      a, a:visited
      {
         color: #0072cf;
      }
      --></style>
   </head>
   
   <body bgcolor="#dddddd">
      <table width="100%" cellpadding="20" cellspacing="0" border="0" bgcolor="#dddddd">
         <tr>
            <td width="100%" align="center">
               <table width="70%" cellpadding="0" cellspacing="0" bgcolor="white" style="background-color: white; border: 1px solid #aaaaaa;">
                  <tr>
                     <td width="100%">
                        <table width="100%" cellpadding="0" cellspacing="0" border="0">
                           <tr>
                              <td style="padding: 10px 30px 0px;">
                                 <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                    <tr>
                                       <td>
                                          <table cellpadding="0" cellspacing="0" border="0">
                                             <tr>
                                                <td>
                                                   <img src="${shareUrl}/res/components/images/task-64.png" alt="" width="64" height="64" border="0" style="padding-right: 20px;" />
                                                </td>
                                                <td>
                                                   <div style="font-size: 22px; padding-bottom: 4px;">
                                                    Вам назначена задача
                                                   </div>
                                                   <div style="font-size: 13px;">
                                                      ${date?datetime?string.full}
                                                   </div>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                              <#if args.workflow.documents??>
                                                  <#if args.workflow.documents?is_sequence>
                                                      <#if args.workflow.documents?size &gt; 0>
                                                          <#assign document=args.workflow.documents[0]>
                                                      </#if>
                                                  </#if>
                                              </#if>

                                              Вам назначена следующая задача:
                                             <#if (args.task.description)?? && args.task.description!=''>                                             
                                                 <p><b>${args.task.description}</b></p>
                                             <#else>
                                                 <#if (args.task.name)??>                                             
                                                     <p><b>${args.task.name}</b></p>
                                                 </#if>
                                             </#if>
                                             
                                             <p>
                                                <#if (args.task.properties.bpm_priority)??>
                                                   Приоритет:&nbsp;&nbsp;
                                                   <b>
                                                   <#if args.task.properties.bpm_priority == 3>
                                                      Низкий
                                                   <#elseif args.task.properties.bpm_priority == 2>
                                                      Средний
                                                   <#else>
                                                      Высокий
                                                   </#if>
                                                   </b><br/>
                                                </#if>
                                                 Задачу необходимо завершить до:&nbsp;&nbsp;
                                                 <b>
                                                 <#if args.task.properties.bpm_dueDate??>
                                                    ${args.task.properties.bpm_dueDate?string("dd.MM.yyyy")!""}.
                                                 <#elseif args.task.properties.bpm_workflowDueDate??>
                                                    ${args.task.properties.bpm_workflowDueDate?string("dd.MM.yyyy")!""}.
                                                 <#else>
                                                     (дата не установлена).
                                                 </#if>
                                                 </b><br/>

                                                 <#if document??>
                                                     Название файла:&nbsp;&nbsp;<b>${document.properties["cm:name"]!''}</b><br>
                                                 </#if>

                                                 Инициатор задачи:&nbsp;&nbsp;
                                                 <b>
                                                 <#if args.task.properties.initiator??>
                                                    ${args.task.properties.initiator.properties["cm:lastName"]} ${args.task.properties.initiator.properties["cm:firstName"]}.
                                                 <#else>
                                                     (Инициатор не указан).
                                                 </#if>
                                                 </b>
                                             </p>

                                              <p>Для редактирования задачи нажмите на ссылку:</p>
                                              <#if document??>
                                                  <#assign taskUrl = shareUrl+"/page/document-details?nodeRef="+document.nodeRef />
                                              <#else>
                                                  <#assign taskUrl = shareUrl+"/page/task-edit?taskId=activiti$"+args.task.id />
                                              </#if>
                                              <p><a href="${taskUrl}">${taskUrl}</a></p>

                                          </div>
                                       </td>
                                    </tr>
                                 </table>
                              </td>
                           </tr>
                        </table>
                     </td>
                  </tr>
               </table>
            </td>
         </tr>
      </table>
   </body>
</html>
</#escape>