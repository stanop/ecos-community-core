<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<#escape x as x?html>
<html>
<body>
<#assign none = "(Нет)" />
<#assign dateFormat = "dd.MM.yyyy" />
<#assign dateTimeFormat = "dd.MM.yyyy HH:mm" />

<h1>Карточка документа ${document.name}</h1>
<hr />

<#assign tableWidth = "700px" />

<table width="${tableWidth}">
	<tr>
		<td width="33%"><em>Имя:</em></td>
		<td width="66%">${document.name}</td>
	</tr>
	<tr>
		<td><em>Путь:</em></td>
		<td>${document.displayPath?replace("/Company Home", "")}</td>
	</tr>
	<tr>
		<td><em>Заголовок:</em></td>
		<td>${document.properties.title!none}</td>
	</tr>
	<tr>
		<td><em>Описание:</em></td>
		<td>${document.properties.description!none}</td>
	</tr>
<#if document.mimetype??>
	<tr>
		<td><em>Тип файла:</em></td>
		<td><em>${document.mimetype}</em></td>
	</tr>
	<tr>
		<td><em>Размер:</em></td>
		<td><em>${(document.size/1024)?round} Кб</em></td>
	</tr>
<#else />
	<tr>
		<td colspan="2"><em>Контент отсутствует</em></td>
	</tr>
</#if>
<#if document.versionHistory?? && document.versionHistory?size != 0>
	<#assign version = document.versionHistory[0] />
	<tr>
		<td><em>Последняя версия:</em></td>
		<td>${version.versionLabel} от ${version.createdDate?string(dateFormat)}</td>
	</tr>
	<tr>
		<td><em>Всего версий:</em></td>
		<td>${document.versionHistory?size?c}</td>
	</tr>
<#else />
	<tr>
		<td><em>Последняя версия:</em></td>
		<td>1.0 от ${document.properties.created?string(dateFormat)}</td>
	</tr>
</#if>
	<tr>
		<td><em>Создан:</em></td>
		<td>${document.properties.created?string(dateTimeFormat)}</td>
	</tr>
	<#assign creator = people.getPerson(document.properties.creator)! />
<#if creator != "">
	<tr>
		<td><em>Пользователем:</em></td>
		<td>
		<#if creator??>
			${creator.properties.firstName!} ${creator.properties.lastName!}
		<#else />
			${document.properties.creator}
		</#if>
		</td>
	</tr>
</#if>
	<tr>
		<td><em>Обновлен:</em></td>
		<td>${document.properties.modified?string(dateTimeFormat)}</td>
	</tr>
	<#assign modifier = people.getPerson(document.properties.modifier)! />
<#if modifier != "">
	<tr>
		<td><em>Пользователем:</em></td>
		<td>
		<#if modifier??>
			${modifier.properties.firstName!} ${modifier.properties.lastName!}
		<#else />
			${document.properties.modifier}
		</#if>
		</td>
	</tr>
</#if>
</table>
	
</body>
</html>
</#escape>
