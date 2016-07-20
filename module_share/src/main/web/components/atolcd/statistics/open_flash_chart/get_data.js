/*
 * Copyright (C) 2013 Atol Conseils et Développements.
 * http://www.atolcd.com/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

function buildTitle(params) {
  return getMessage(params.additionalsParams.type, "graph.title.") + buildDateTitle(params);
}

function displayNodeDetailsPopup(itemStr) {  // Will be drop soon.
  var item = YAHOO.lang.JSON.parse(unescape(itemStr));

  // TODO: make something cleaner?
  var body = '<div class="node-details-popup">';
  body += '<p><label>' + getMessage("label.popup.filename") + '</label>' + item.displayName + '</p>';
  body += (item.siteTitle) ? '<p><label>' + getMessage("label.popup.type") + '</label>' + getMessage("site.component." + item.siteComponent) + '</p>' : '';
  body += (item.siteTitle) ? '<p><label>' + getMessage("label.menu.site") + '</label>' + item.siteTitle + '</p>' : '';
  body += '<p><label>' + getMessage("label.popup.hits") + '</label>' + item.popularity + '</p>';
  body += '</div>';

  Alfresco.util.PopupManager.displayPrompt({
    title: item.displayName,
    text: body,
    close: true,
    noEscape: true,
    buttons: [{
      text: getMessage("button.go-to-node-page"),
      handler:{
        fn: function(e, param) {
          window.open(param.url);
          this.destroy();
        },
        obj: item
      }
    }, {
      text: getMessage("button.cancel"),
      handler: function () {
        this.destroy();
      },
      isDefault: true
    }]
  });
}


function getMessage(messageId, prefix) {
  var msg = (prefix) ? prefix + messageId : messageId;
  var res = Alfresco.util.message.call(null, msg, "AtolStatistics.GlobalUsage", Array.prototype.slice.call(arguments).slice(2));
  res = (res.search("graph.label") == 0) ? messageId : res;
  return res;
}