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
                                                     Задача просрочена
                                                   </div>
                                                   <div style="font-size: 13px;">
                                                      ${date?datetime?string.full}
                                                   </div>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;"> 
Срок исполнения задачи истек. 
<b>Необходимо рассмотреть в ближайшее время.</b>

                                             <#if (args.task.properties.bpm_description)??>                                             
                                             	<p>${args.task.properties.bpm_description}</p>                                             
                                             </#if>
                                             
                                             <p>
                                                <#if (args.task.properties.bpm_dueDate)??>Срок:&nbsp;&nbsp;<b>${args.task.properties.bpm_dueDate?date?string.full}</b><br></#if>
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
                                                   </b>
                                                </#if>
                                             </p>

											 <p>Для редактирования задачи нажмите на ссылку:</p>
											 <#assign taskUrl = shareUrl+"/page/task-edit-with-preview?taskId="+args.task.id/>
                                             <#if args.workflow.documents?? && args.workflow.documents?size != 0><#assign taskUrl = taskUrl+"&nodeRef="+args.workflow.documents[0].nodeRef/></#if>
											 <p><a href="${taskUrl}">${taskUrl}</a> </p>
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